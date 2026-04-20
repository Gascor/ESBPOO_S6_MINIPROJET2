package com.leadelmarche.ui.mvc.controller;

import com.leadelmarche.domain.promotion.PromotionRule;
import com.leadelmarche.service.PromotionCatalogService;
import com.leadelmarche.ui.mvc.view.PromotionView;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import javax.swing.JOptionPane;

public class PromotionController {
    private final PromotionCatalogService promotionCatalogService;
    private final PromotionView view;

    public PromotionController(PromotionCatalogService promotionCatalogService) {
        this.promotionCatalogService = promotionCatalogService;
        this.view = new PromotionView();
        bindActions();
        refresh();
    }

    public void show() {
        view.setVisible(true);
    }

    private void bindActions() {
        view.addButton().addActionListener(e -> createPromotion());
        view.deactivateButton().addActionListener(e -> deactivatePromotion());
        view.refreshButton().addActionListener(e -> refresh());
    }

    private void createPromotion() {
        try {
            promotionCatalogService.createPromotion(
                view.nameField().getText().trim(),
                view.productIdField().getText().trim(),
                LocalDate.parse(view.startField().getText().trim()),
                LocalDate.parse(view.endField().getText().trim()),
                view.renewableBox().isSelected(),
                view.effectCombo().getItemAt(view.effectCombo().getSelectedIndex()),
                view.typeCombo().getItemAt(view.typeCombo().getSelectedIndex()),
                parseInt(view.buyQtyField().getText().trim(), 2),
                parseInt(view.freeQtyField().getText().trim(), 1),
                parseInt(view.nthItemField().getText().trim(), 2),
                parseDecimal(view.percentField().getText().trim(), BigDecimal.valueOf(30))
            );
            refresh();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void deactivatePromotion() {
        try {
            String id = view.deactivateIdField().getText().trim();
            if (id.isBlank()) {
                throw new IllegalArgumentException("ID promotion obligatoire");
            }
            promotionCatalogService.deactivatePromotion(id);
            refresh();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void refresh() {
        List<PromotionRule> rules = promotionCatalogService.listPromotions(false);
        StringBuilder sb = new StringBuilder();
        sb.append("ID | ACTIF | NOM | TYPE | EFFET | PERIODE | PRODUCT_ID\n");
        sb.append("-------------------------------------------------------------------------------\n");
        for (PromotionRule rule : rules) {
            sb.append(rule.getId()).append(" | ")
                .append(rule.isActive()).append(" | ")
                .append(rule.getName()).append(" | ")
                .append(rule.getType()).append(" | ")
                .append(rule.getEffect()).append(" | ")
                .append(rule.getStartDate()).append(" -> ").append(rule.getEndDate()).append(" | ")
                .append(rule.getProductId()).append('\n');
            if (rule.getType().name().equals("BUY_X_GET_Y")) {
                sb.append("  Parametres: buy=").append(rule.getBuyQty()).append(" free=").append(rule.getFreeQty()).append('\n');
            } else {
                sb.append("  Parametres: nth=").append(rule.getNthItem()).append(" percent=").append(rule.getPercentDiscount()).append('\n');
            }
        }
        view.outputArea().setText(sb.toString());
    }

    private int parseInt(String text, int fallback) {
        try {
            return Integer.parseInt(text);
        } catch (Exception ex) {
            return fallback;
        }
    }

    private BigDecimal parseDecimal(String text, BigDecimal fallback) {
        try {
            return new BigDecimal(text);
        } catch (Exception ex) {
            return fallback;
        }
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(view, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
    }
}
