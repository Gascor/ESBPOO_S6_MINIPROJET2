package com.leadelmarche.service;

import com.leadelmarche.domain.inventory.InventoryItem;
import com.leadelmarche.domain.inventory.Product;
import com.leadelmarche.domain.inventory.ProductType;
import com.leadelmarche.persistence.InventoryRepository;
import com.leadelmarche.persistence.ProductRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InventoryService {
    public static final String DEFAULT_STORE_ID = "STORE-001";

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    public InventoryService(ProductRepository productRepository, InventoryRepository inventoryRepository) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }

    public Product createProduct(Product product) {
        return productRepository.create(product);
    }

    public Product updateProduct(Product product) {
        return productRepository.update(product);
    }

    public void deactivateProduct(String productId) {
        productRepository.findById(productId).ifPresent(p -> {
            // Soft delete: on masque la fiche au lieu de supprimer l'historique.
            p.setActive(false);
            productRepository.update(p);
        });
    }

    public List<Product> listProducts(boolean activeOnly) {
        return productRepository.findAll(activeOnly);
    }

    public List<Product> searchProducts(String partial) {
        return productRepository.searchByName(partial);
    }

    public Optional<Product> findProductById(String id) {
        return productRepository.findById(id);
    }

    public Optional<Product> findByBarcode(String barcode) {
        return productRepository.findByBarcode(barcode);
    }

    public Optional<Product> findWeightedProductByType(ProductType type) {
        // Retourne le premier produit actif vendable au poids pour ce type.
        return productRepository.findAll(true).stream()
            .filter(p -> p.isWeighted() && p.getType() == type)
            .findFirst();
    }

    public List<Product> listWeightedProducts() {
        return productRepository.findAll(true).stream()
            .filter(Product::isWeighted)
            .toList();
    }

    public List<Product> listPieceProductsWithoutBarcode() {
        return productRepository.findAll(true).stream()
            .filter(Product::isSoldByPieceWithoutBarcode)
            .toList();
    }

    public void addStock(String productId, String storeId, BigDecimal quantity) {
        if (quantity.signum() < 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        // Crée une ligne d'inventaire si ce couple (produit, magasin) n'existe pas encore.
        InventoryItem item = inventoryRepository.findByProductAndStore(productId, storeId).orElseGet(() -> {
            InventoryItem created = new InventoryItem();
            created.setProductId(productId);
            created.setStoreId(storeId);
            created.setQuantity(BigDecimal.ZERO);
            created.setReservedQuantity(BigDecimal.ZERO);
            return created;
        });
        item.setQuantity(item.getQuantity().add(quantity));
        // Premier passage = create, sinon update.
        if (item.getId() == null || item.getId().isBlank()) {
            inventoryRepository.create(item);
        } else {
            inventoryRepository.update(item);
        }
    }

    public void removeStock(String productId, String storeId, BigDecimal quantity) {
        if (quantity.signum() < 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        // Refuse toute sortie de stock qui passerait sous zero.
        InventoryItem item = inventoryRepository.findByProductAndStore(productId, storeId).orElseThrow(
            () -> new IllegalStateException("No inventory record for product " + productId + " in store " + storeId)
        );
        BigDecimal newQty = item.getQuantity().subtract(quantity);
        if (newQty.signum() < 0) {
            throw new IllegalStateException("Insufficient stock for product " + productId);
        }
        item.setQuantity(newQty);
        inventoryRepository.update(item);
    }

    public BigDecimal getAvailableStock(String productId, String storeId) {
        // Quantité disponible = quantité totale - quantité réservée.
        return inventoryRepository.findByProductAndStore(productId, storeId)
            .map(InventoryItem::availableQuantity)
            .orElse(BigDecimal.ZERO);
    }

    public List<InventoryItem> getStockAcrossStores(String productId) {
        // Base pour la vue de consultation inter-succursales.
        return inventoryRepository.findAll(true).stream()
            .filter(item -> productId.equals(item.getProductId()))
            .toList();
    }

    public List<String> detectLowStockAlerts() {
        List<String> alerts = new ArrayList<>();
        List<Product> products = productRepository.findAll(true);
        for (Product p : products) {
            BigDecimal stock = getAvailableStock(p.getId(), DEFAULT_STORE_ID);
            // MVP: ce "percent" est utilisé comme seuil mini en unités dans la succursale par défaut.
            BigDecimal thresholdUnits = p.getLowStockThresholdPercent().max(BigDecimal.ONE);
            if (stock.compareTo(thresholdUnits) <= 0) {
                alerts.add("LOW STOCK: " + p.getName() + " (" + stock + ")");
            }
        }
        return alerts;
    }
}
