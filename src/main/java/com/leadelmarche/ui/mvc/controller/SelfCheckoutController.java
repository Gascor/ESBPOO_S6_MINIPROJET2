package com.leadelmarche.ui.mvc.controller;

import com.leadelmarche.domain.inventory.Product;
import com.leadelmarche.domain.sales.PaymentMode;
import com.leadelmarche.domain.sales.Receipt;
import com.leadelmarche.domain.sales.Sale;
import com.leadelmarche.domain.sales.SaleLine;
import com.leadelmarche.service.InventoryService;
import com.leadelmarche.service.SalesService;
import com.leadelmarche.ui.mvc.view.SelfCheckoutView;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.JOptionPane;

public class SelfCheckoutController {
    private final SalesService salesService;
    private final InventoryService inventoryService;
    private final SelfCheckoutView view;
    private String currentSaleId;

    private record ProductChoice(String id, String label) {
        @Override
        public String toString() {
            return label;
        }
    }

    public SelfCheckoutController(SalesService salesService, InventoryService inventoryService) {
        this.salesService = salesService;
        this.inventoryService = inventoryService;
        this.view = new SelfCheckoutView();
        loadProductChoices();
        bindActions();
    }

    public void show() {
        view.setVisible(true);
    }

    private void bindActions() {
        view.startSaleButton().addActionListener(e -> startSale());
        view.addScanButton().addActionListener(e -> addByScan());
        view.addPieceButton().addActionListener(e -> addPieceProduct());
        view.addWeightedButton().addActionListener(e -> addWeightedProduct());
        view.helpButton().addActionListener(e -> callCashier());
        view.finalizeButton().addActionListener(e -> finalizeSale());
    }

    private void loadProductChoices() {
        view.pieceProductCombo().removeAllItems();
        for (Product product : inventoryService.listPieceProductsWithoutBarcode()) {
            view.pieceProductCombo().addItem(new ProductChoice(product.getId(), product.getName() + " [" + product.getSku() + "]"));
        }

        view.weightedProductCombo().removeAllItems();
        for (Product product : inventoryService.listWeightedProducts()) {
            view.weightedProductCombo().addItem(new ProductChoice(product.getId(), product.getName() + " [" + product.getSku() + "]"));
        }
    }

    private void startSale() {
        try {
            Sale sale = salesService.startSale(
                view.posIdField().getText().trim(),
                "SELF-CHECKOUT",
                view.customerCardField().getText().trim()
            );
            currentSaleId = sale.getId();
            view.saleIdLabel().setText("Sale ID: " + currentSaleId + " / " + sale.getSaleNumber());
            renderDraft("Caisse automatique demarree");
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void addByScan() {
        try {
            ensureSaleStarted();
            salesService.addLineByBarcodeWithMeasuredWeight(
                currentSaleId,
                view.barcodeField().getText().trim(),
                parseBigDecimal(view.qtyField().getText(), BigDecimal.ONE),
                parseBigDecimal(view.measuredWeightField().getText(), BigDecimal.ZERO)
            );
            renderDraft("Produit scanne et valide");
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void addPieceProduct() {
        try {
            ensureSaleStarted();
            ProductChoice selected = (ProductChoice) view.pieceProductCombo().getSelectedItem();
            if (selected == null) {
                throw new IllegalStateException("Aucun produit a la piece disponible");
            }
            salesService.addLineByProductId(
                currentSaleId,
                selected.id(),
                parseBigDecimal(view.pieceQtyField().getText(), BigDecimal.ONE)
            );
            renderDraft("Produit a la piece ajoute");
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void addWeightedProduct() {
        try {
            ensureSaleStarted();
            ProductChoice selected = (ProductChoice) view.weightedProductCombo().getSelectedItem();
            if (selected == null) {
                throw new IllegalStateException("Aucun produit au poids disponible");
            }
            salesService.addWeightedLineByProductId(
                currentSaleId,
                selected.id(),
                parseBigDecimal(view.manualWeightField().getText(), BigDecimal.ONE)
            );
            renderDraft("Produit au poids ajoute");
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void callCashier() {
        try {
            ensureSaleStarted();
            String message = salesService.requestCashierHelp(currentSaleId, "Assistance demandee en caisse automatique");
            JOptionPane.showMessageDialog(view, message, "Assistance", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void finalizeSale() {
        try {
            ensureSaleStarted();
            PaymentMode mode = PaymentMode.valueOf(view.paymentModeCombo().getSelectedItem().toString());
            salesService.changePaymentMode(currentSaleId, mode);
            Receipt receipt = salesService.finalizeSale(currentSaleId);
            view.cartArea().setText(receipt.getTextBody());
            currentSaleId = null;
            view.saleIdLabel().setText("Sale ID: -");
            JOptionPane.showMessageDialog(view, "Paiement finalise. Ticket cree.", "OK", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void renderDraft(String header) {
        Sale sale = salesService.getSale(currentSaleId);
        List<SaleLine> lines = salesService.getDraftLines(currentSaleId);
        StringBuilder sb = new StringBuilder();
        sb.append(header).append('\n');
        sb.append("Vente: ").append(sale.getSaleNumber()).append('\n');
        sb.append("Paiement: ").append(sale.getPaymentMode()).append('\n');
        sb.append("------------------------------------------\n");
        for (SaleLine line : lines) {
            sb.append(line.getProductName()).append(" x").append(line.getQuantity()).append(" | TTC ").append(line.getLineTotal()).append('\n');
        }
        sb.append("------------------------------------------\n");
        sb.append("Sous-total HT: ").append(sale.getSubTotalHT()).append('\n');
        sb.append("TVA: ").append(sale.getTotalVat()).append('\n');
        sb.append("Total TTC: ").append(sale.getTotalTTC()).append('\n');
        view.cartArea().setText(sb.toString());
    }

    private void ensureSaleStarted() {
        if (currentSaleId == null || currentSaleId.isBlank()) {
            throw new IllegalStateException("Demarrer une vente avant toute action.");
        }
    }

    private BigDecimal parseBigDecimal(String value, BigDecimal fallback) {
        try {
            return new BigDecimal(value.trim());
        } catch (Exception ex) {
            return fallback;
        }
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(view, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
    }
}
