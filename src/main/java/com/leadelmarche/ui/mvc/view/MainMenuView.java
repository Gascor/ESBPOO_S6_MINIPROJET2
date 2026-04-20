package com.leadelmarche.ui.mvc.view;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

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
        setSize(420, 340);
        setLocationRelativeTo(null);

        JLabel title = new JLabel("Selectionner un module", SwingConstants.CENTER);
        JPanel buttons = new JPanel(new GridLayout(6, 1, 8, 8));
        buttons.add(stockButton);
        buttons.add(customerButton);
        buttons.add(staffButton);
        buttons.add(posButton);
        buttons.add(statsButton);
        buttons.add(exitButton);

        add(title, BorderLayout.NORTH);
        add(buttons, BorderLayout.CENTER);
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
