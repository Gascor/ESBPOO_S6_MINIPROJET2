package com.leadelmarche.app;

import com.leadelmarche.ui.mvc.controller.MainMenuController;
import javax.swing.SwingUtilities;

public final class LeadelMarcheApp {
    private LeadelMarcheApp() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ApplicationContext context = ApplicationContext.getInstance();
            MainMenuController mainMenuController = new MainMenuController(context);
            mainMenuController.show();
        });
    }
}

