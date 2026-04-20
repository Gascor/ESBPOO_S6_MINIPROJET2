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

public class StaffView extends JFrame {
    private final JTextField badgeField = new JTextField();
    private final JTextField firstNameField = new JTextField();
    private final JTextField lastNameField = new JTextField();
    private final JTextField roleField = new JTextField();
    private final JTextField supervisorField = new JTextField();
    private final JButton addButton = new JButton("Ajouter Employe");
    private final JTextField searchField = new JTextField();
    private final JButton searchButton = new JButton("Rechercher");
    private final JButton refreshButton = new JButton("Rafraichir");
    private final JTextArea outputArea = new JTextArea();

    public StaffView() {
        super("LeadelMarche - Personnel");
        setSize(1000, 1000);
        setLocationRelativeTo(null);
        Branding.applyWindowIcon(this);

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Badge"));
        form.add(badgeField);
        form.add(new JLabel("Prenom"));
        form.add(firstNameField);
        form.add(new JLabel("Nom"));
        form.add(lastNameField);
        form.add(new JLabel("Role"));
        form.add(roleField);
        form.add(new JLabel("Superieur badge"));
        form.add(supervisorField);
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

        add(Branding.createHeader("Module Personnel"), BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
    }

    public JTextField badgeField() { return badgeField; }
    public JTextField firstNameField() { return firstNameField; }
    public JTextField lastNameField() { return lastNameField; }
    public JTextField roleField() { return roleField; }
    public JTextField supervisorField() { return supervisorField; }
    public JButton addButton() { return addButton; }
    public JTextField searchField() { return searchField; }
    public JButton searchButton() { return searchButton; }
    public JButton refreshButton() { return refreshButton; }
    public JTextArea outputArea() { return outputArea; }
}
