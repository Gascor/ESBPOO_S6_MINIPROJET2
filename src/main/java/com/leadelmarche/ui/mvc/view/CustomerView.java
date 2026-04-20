package com.leadelmarche.ui.mvc.view;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class CustomerView extends JFrame {
    private final JTextField firstNameField = new JTextField();
    private final JTextField lastNameField = new JTextField();
    private final JTextField cardField = new JTextField();
    private final JTextField emailField = new JTextField();
    private final JTextField postalField = new JTextField();
    private final JButton addButton = new JButton("Ajouter Client");
    private final JTextField searchField = new JTextField();
    private final JButton searchButton = new JButton("Rechercher");
    private final JButton refreshButton = new JButton("Rafraichir");
    private final JTextArea outputArea = new JTextArea();

    public CustomerView() {
        super("LeadelMarche - Clients");
        setSize(1000, 1000);
        setLocationRelativeTo(null);
        Branding.applyWindowIcon(this);

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Prenom"));
        form.add(firstNameField);
        form.add(new JLabel("Nom"));
        form.add(lastNameField);
        form.add(new JLabel("Carte fidelite"));
        form.add(cardField);
        form.add(new JLabel("Email"));
        form.add(emailField);
        form.add(new JLabel("Code postal"));
        form.add(postalField);
        form.add(new JLabel(""));
        form.add(addButton);

        JPanel search = new JPanel(new GridLayout(1, 3, 6, 6));
        search.add(searchField);
        search.add(searchButton);
        search.add(refreshButton);

        JPanel north = new JPanel(new BorderLayout());
        north.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        north.add(form, BorderLayout.CENTER);
        north.add(search, BorderLayout.SOUTH);

        outputArea.setEditable(false);

        JPanel content = new JPanel(new BorderLayout(0, 6));
        content.add(north, BorderLayout.NORTH);
        content.add(new JScrollPane(outputArea), BorderLayout.CENTER);

        add(Branding.createHeader("Module Clients"), BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
    }

    public JTextField firstNameField() { return firstNameField; }
    public JTextField lastNameField() { return lastNameField; }
    public JTextField cardField() { return cardField; }
    public JTextField emailField() { return emailField; }
    public JTextField postalField() { return postalField; }
    public JButton addButton() { return addButton; }
    public JTextField searchField() { return searchField; }
    public JButton searchButton() { return searchButton; }
    public JButton refreshButton() { return refreshButton; }
    public JTextArea outputArea() { return outputArea; }
}
