package com.leadelmarche.ui.mvc.view;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class MainMenuView extends JFrame {
    private final JButton stockButton = new JButton("Module Stock");
    private final JButton customerButton = new JButton("Module Clients");
    private final JButton staffButton = new JButton("Module Personnel");
    private final JButton posButton = new JButton("Point de Vente");
    private final JButton statsButton = new JButton("Module Statistiques");
    private final JButton exitButton = new JButton("Quitter");

    public MainMenuView() {
        super("LeadelMarche - Menu principal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(550, 350);
        setLocationRelativeTo(null);
        Branding.applyWindowIcon(this);

        JPanel buttons = new JPanel(new GridLayout(6, 1, 8, 8));
        buttons.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttons.add(stockButton);
        buttons.add(customerButton);
        buttons.add(staffButton);
        buttons.add(posButton);
        buttons.add(statsButton);
        buttons.add(exitButton);

        JPanel content = new JPanel(new BorderLayout());
        content.add(buttons, BorderLayout.CENTER);

        add(Branding.createHeader("Selectionner un module"), BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
    }

    public JButton stockButton() {
        return stockButton;
    }

    public JButton customerButton() {
        return customerButton;
    }

    public JButton staffButton() {
        return staffButton;
    }

    public JButton posButton() {
        return posButton;
    }

    public JButton statsButton() {
        return statsButton;
    }

    public JButton exitButton() {
        return exitButton;
    }
}
