package com.leadelmarche.ui.mvc.controller;

import com.leadelmarche.app.ApplicationContext;
import com.leadelmarche.ui.mvc.view.MainMenuView;
import javax.swing.JOptionPane;

public class MainMenuController {
    private final ApplicationContext context;
    private final MainMenuView view;

    public MainMenuController(ApplicationContext context) {
        this.context = context;
        this.view = new MainMenuView();
        bindActions();
    }

    public void show() {
        view.setVisible(true);
    }

    private void bindActions() {
        view.stockButton().addActionListener(e -> new StockController(context.inventoryService()).show());
        view.customerButton().addActionListener(e -> new CustomerController(context.customerService()).show());
        view.staffButton().addActionListener(
            e -> new StaffController(context.staffService(), context.absenceService(), context.scheduleService()).show()
        );
        view.posButton().addActionListener(e -> openCheckoutModule());
        view.promotionButton().addActionListener(e -> new PromotionController(context.promotionCatalogService()).show());
        view.statsButton().addActionListener(e -> new StatsController(context.statisticsService(), context.mailOutboxService()).show());
        view.exitButton().addActionListener(e -> System.exit(0));
    }

    private void openCheckoutModule() {
        Object[] options = {"Caisse normale", "Caisse automatique", "Annuler"};
        int choice = JOptionPane.showOptionDialog(
            view,
            "Choisir le mode de caisse",
            "Point de Vente",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );
        if (choice == 0) {
            String badge = JOptionPane.showInputDialog(view, "Badge caissier:");
            if (badge == null || badge.trim().isBlank()) {
                return;
            }
            new POSController(context.salesService(), badge.trim()).show();
            return;
        }
        if (choice == 1) {
            new SelfCheckoutController(context.salesService(), context.inventoryService()).show();
        }
    }
}
