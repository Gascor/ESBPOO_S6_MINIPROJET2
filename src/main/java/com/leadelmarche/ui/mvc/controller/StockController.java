package com.leadelmarche.ui.mvc.controller;

import com.leadelmarche.domain.inventory.Product;
import com.leadelmarche.domain.inventory.ProductType;
import com.leadelmarche.service.InventoryService;
import com.leadelmarche.ui.mvc.view.StockView;
import java.math.BigDecimal;
import java.util.List;
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
        view.restockButton().addActionListener(e -> restockProduct());
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
            BigDecimal qty = parseBigDecimal(view.restockQtyField().getText(), BigDecimal.ZERO);
            inventoryService.addStock(productId, InventoryService.DEFAULT_STORE_ID, qty);
            refreshProducts();
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

