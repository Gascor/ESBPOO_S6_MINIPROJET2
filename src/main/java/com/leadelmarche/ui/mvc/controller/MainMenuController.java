package com.leadelmarche.ui.mvc.controller;

import com.leadelmarche.app.ApplicationContext;
import com.leadelmarche.ui.mvc.view.MainMenuView;

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
        view.staffButton().addActionListener(e -> new StaffController(context.staffService()).show());
        view.posButton().addActionListener(e -> new POSController(context.salesService()).show());
        view.statsButton().addActionListener(e -> new StatsController(context.statisticsService()).show());
        view.exitButton().addActionListener(e -> System.exit(0));
    }
}
