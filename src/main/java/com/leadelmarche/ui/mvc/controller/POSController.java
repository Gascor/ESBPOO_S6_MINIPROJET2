package com.leadelmarche.ui.mvc.controller;

import com.leadelmarche.domain.inventory.ProductType;
import com.leadelmarche.domain.sales.PaymentMode;
import com.leadelmarche.domain.sales.Receipt;
import com.leadelmarche.domain.sales.Sale;
import com.leadelmarche.domain.sales.SaleLine;
import com.leadelmarche.service.SalesService;
import com.leadelmarche.ui.mvc.view.POSView;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.JOptionPane;

public class POSController {
    private final SalesService salesService;
    private final POSView view;
    private String currentSaleId;

    public POSController(SalesService salesService) {
        this(salesService, null);
    }

    public POSController(SalesService salesService, String prefilledBadge) {
        this.salesService = salesService;
        this.view = new POSView();
        if (prefilledBadge != null && !prefilledBadge.isBlank()) {
            this.view.setBadgePrefill(prefilledBadge, false);
        }
        bindActions();
    }

    public void show() {
        view.setVisible(true);
    }

    private void bindActions() {
        view.startSaleButton().addActionListener(e -> startSale());
        view.addBarcodeButton().addActionListener(e -> addByBarcode());
        view.addWeightedButton().addActionListener(e -> addWeighted());
        view.changePaymentButton().addActionListener(e -> changePaymentMode());
        view.finalizeButton().addActionListener(e -> finalizeSale());
    }

    private void startSale() {
        try {
            Sale sale = salesService.startSale(
                view.posIdField().getText().trim(),
                view.badgeField().getText().trim(),
                view.customerCardField().getText().trim()
            );
            currentSaleId = sale.getId();
            view.saleIdLabel().setText("Sale ID: " + currentSaleId + " / " + sale.getSaleNumber());
            renderDraft("Vente demarree");
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void addByBarcode() {
        try {
            ensureSaleStarted();
            salesService.addLineByBarcode(
                currentSaleId,
                view.barcodeField().getText().trim(),
                parseBigDecimal(view.qtyField().getText(), BigDecimal.ONE)
            );
            renderDraft("Ligne ajoutee par code-barres");
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void addWeighted() {
        try {
            ensureSaleStarted();
            ProductType type = ProductType.valueOf(view.weightedTypeCombo().getSelectedItem().toString());
            salesService.addWeightedLine(
                currentSaleId,
                type,
                parseBigDecimal(view.weightField().getText(), BigDecimal.ONE)
            );
            renderDraft("Ligne pesee ajoutee");
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void changePaymentMode() {
        try {
            ensureSaleStarted();
            PaymentMode mode = PaymentMode.valueOf(view.paymentModeCombo().getSelectedItem().toString());
            salesService.changePaymentMode(currentSaleId, mode);
            renderDraft("Mode paiement mis a jour: " + mode);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void finalizeSale() {
        try {
            ensureSaleStarted();
            Receipt receipt = salesService.finalizeSale(currentSaleId);
            view.cartArea().setText(receipt.getTextBody());
            currentSaleId = null;
            view.saleIdLabel().setText("Sale ID: -");
            JOptionPane.showMessageDialog(view, "Vente finalisee. Ticket cree.", "OK", JOptionPane.INFORMATION_MESSAGE);
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
            sb.append(line.getId()).append(" | ")
                .append(line.getProductName()).append(" x")
                .append(line.getQuantity()).append(" | TTC ")
                .append(line.getLineTotal()).append('\n');
        }
        sb.append("------------------------------------------\n");
        sb.append("Sous-total HT: ").append(sale.getSubTotalHT()).append('\n');
        sb.append("TVA: ").append(sale.getTotalVat()).append('\n');
        sb.append("Total TTC: ").append(sale.getTotalTTC()).append('\n');
        view.cartArea().setText(sb.toString());
    }

    private void ensureSaleStarted() {
        if (currentSaleId == null || currentSaleId.isBlank()) {
            throw new IllegalStateException("Demarrer une vente avant d'ajouter des lignes.");
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
