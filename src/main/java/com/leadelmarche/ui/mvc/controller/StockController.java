package com.leadelmarche.ui.mvc.controller;

import com.leadelmarche.domain.inventory.InventoryItem;
import com.leadelmarche.domain.inventory.Product;
import com.leadelmarche.domain.inventory.ProductType;
import com.leadelmarche.service.InventoryService;
import com.leadelmarche.ui.mvc.view.StockView;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import javax.swing.JOptionPane;

public class StockController {
    private final InventoryService inventoryService;
    private final StockView view;

    public StockController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
        this.view = new StockView();
        bindActions();
        refreshProducts();
    }

    public void show() {
        view.setVisible(true);
    }

    private void bindActions() {
        view.addProductButton().addActionListener(e -> addProduct());
        view.updateProductButton().addActionListener(e -> updateProduct());
        view.deactivateProductButton().addActionListener(e -> deactivateProduct());
        view.restockButton().addActionListener(e -> restockProduct());
        view.interStoreButton().addActionListener(e -> showInterStoreStock());
        view.refreshButton().addActionListener(e -> refreshProducts());
        view.searchButton().addActionListener(e -> searchProducts());
    }

    private void addProduct() {
        try {
            Product p = new Product();
            p.setSku(view.skuField().getText().trim());
            p.setName(view.nameField().getText().trim());
            p.setBarcode(view.barcodeField().getText().trim());
            p.setDescription("Produit cree depuis l'interface");
            p.setType(ProductType.valueOf(view.typeCombo().getSelectedItem().toString()));
            p.setUnitPriceHT(parseBigDecimal(view.priceField().getText(), BigDecimal.ZERO));
            p.setVatPercent(parseBigDecimal(view.vatField().getText(), BigDecimal.ZERO));
            p.setCountryOfOrigin("FR");
            p.setSupplierName("Fournisseur UI");
            p.setWeighted(view.weightedBox().isSelected());
            p.setSoldByPieceWithoutBarcode(view.pieceNoBarcodeBox().isSelected());
            p.setExpectedWeightKg(BigDecimal.ONE);
            p.setLowStockThresholdPercent(BigDecimal.valueOf(20));
            Product created = inventoryService.createProduct(p);
            inventoryService.addStock(
                created.getId(),
                InventoryService.DEFAULT_STORE_ID,
                parseBigDecimal(view.initStockField().getText(), BigDecimal.ZERO)
            );
            refreshProducts();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void restockProduct() {
        try {
            String productId = view.restockProductIdField().getText().trim();
            String storeId = view.restockStoreIdField().getText().trim();
            if (storeId.isBlank()) {
                storeId = InventoryService.DEFAULT_STORE_ID;
            }
            BigDecimal qty = parseBigDecimal(view.restockQtyField().getText(), BigDecimal.ZERO);
            inventoryService.addStock(productId, storeId, qty);
            refreshProducts();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void updateProduct() {
        try {
            String productId = view.editProductIdField().getText().trim();
            if (productId.isBlank()) {
                throw new IllegalArgumentException("ID produit obligatoire pour la mise a jour");
            }
            Product product = inventoryService.findProductById(productId).orElseThrow(
                () -> new IllegalStateException("Produit introuvable: " + productId)
            );
            if (!view.skuField().getText().trim().isBlank()) {
                product.setSku(view.skuField().getText().trim());
            }
            if (!view.nameField().getText().trim().isBlank()) {
                product.setName(view.nameField().getText().trim());
            }
            if (!view.barcodeField().getText().trim().isBlank()) {
                product.setBarcode(view.barcodeField().getText().trim());
            }
            product.setType(ProductType.valueOf(view.typeCombo().getSelectedItem().toString()));
            if (!view.priceField().getText().trim().isBlank()) {
                product.setUnitPriceHT(parseBigDecimal(view.priceField().getText(), product.getUnitPriceHT()));
            }
            if (!view.vatField().getText().trim().isBlank()) {
                product.setVatPercent(parseBigDecimal(view.vatField().getText(), product.getVatPercent()));
            }
            product.setWeighted(view.weightedBox().isSelected());
            product.setSoldByPieceWithoutBarcode(view.pieceNoBarcodeBox().isSelected());
            inventoryService.updateProduct(product);
            refreshProducts();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void deactivateProduct() {
        try {
            String productId = view.editProductIdField().getText().trim();
            if (productId.isBlank()) {
                throw new IllegalArgumentException("ID produit obligatoire pour la desactivation");
            }
            inventoryService.deactivateProduct(productId);
            refreshProducts();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void showInterStoreStock() {
        try {
            String productId = view.interStoreProductIdField().getText().trim();
            if (productId.isBlank()) {
                // Petit confort: reutilise l'ID de la zone CRUD si l'utilisateur ne remplit pas ce champ.
                productId = view.editProductIdField().getText().trim();
            }
            if (productId.isBlank()) {
                throw new IllegalArgumentException("Saisir un Product ID pour consulter les succursales");
            }
            List<InventoryItem> items = inventoryService.getStockAcrossStores(productId);
            Optional<Product> product = inventoryService.findProductById(productId);
            StringBuilder sb = new StringBuilder();
            sb.append("STOCK INTER-SUCCURSALES\n");
            sb.append("Produit: ").append(product.map(Product::getName).orElse("Inconnu")).append(" (").append(productId).append(")\n");
            sb.append("Store | Quantite | Reserve | Disponible\n");
            sb.append("-------------------------------------------------\n");
            if (items.isEmpty()) {
                sb.append("Aucune donnee inter-succursales pour ce produit.\n");
            } else {
                // Vue consolidée multi magasins pour aider l'orientation client vers le point de vente dispo.
                for (InventoryItem item : items) {
                    sb.append(item.getStoreId()).append(" | ")
                        .append(item.getQuantity()).append(" | ")
                        .append(item.getReservedQuantity()).append(" | ")
                        .append(item.availableQuantity()).append('\n');
                }
            }
            view.outputArea().setText(sb.toString());
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void searchProducts() {
        List<Product> products = inventoryService.searchProducts(view.searchField().getText());
        renderProducts(products);
    }

    private void refreshProducts() {
        renderProducts(inventoryService.listProducts(true));
    }

    private void renderProducts(List<Product> products) {
        StringBuilder sb = new StringBuilder();
        sb.append("ID | SKU | NOM | BARCODE | TYPE | PRIX HT | TVA | STOCK\n");
        sb.append("--------------------------------------------------------------------------\n");
        for (Product p : products) {
            BigDecimal stock = inventoryService.getAvailableStock(p.getId(), InventoryService.DEFAULT_STORE_ID);
            sb.append(p.getId()).append(" | ")
                .append(p.getSku()).append(" | ")
                .append(p.getName()).append(" | ")
                .append(p.getBarcode()).append(" | ")
                .append(p.getType()).append(" | ")
                .append(p.getUnitPriceHT()).append(" | ")
                .append(p.getVatPercent()).append(" | ")
                .append(stock).append('\n');
        }
        List<String> alerts = inventoryService.detectLowStockAlerts();
        if (!alerts.isEmpty()) {
            sb.append("\nALERTES:\n");
            alerts.forEach(a -> sb.append("- ").append(a).append('\n'));
        }
        view.outputArea().setText(sb.toString());
    }

    private BigDecimal parseBigDecimal(String text, BigDecimal fallback) {
        try {
            return new BigDecimal(text.trim());
        } catch (Exception ex) {
            return fallback;
        }
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(view, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
    }
}
