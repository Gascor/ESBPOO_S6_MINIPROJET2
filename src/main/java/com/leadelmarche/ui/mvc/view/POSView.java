package com.leadelmarche.ui.mvc.view;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class POSView extends JFrame {
    private final JTextField posIdField = new JTextField("POS-001");
    private final JTextField badgeField = new JTextField();
    private final JTextField customerCardField = new JTextField();
    private final JButton startSaleButton = new JButton("Demarrer Vente");
    private final JLabel saleIdLabel = new JLabel("Sale ID: -");

    private final JTextField barcodeField = new JTextField();
    private final JTextField qtyField = new JTextField("1");
    private final JButton addBarcodeButton = new JButton("Ajouter par code-barres");

    private final JComboBox<String> weightedTypeCombo = new JComboBox<>(new String[]{"ALIMENTAIRE", "MEDECINE", "ELECTRONIQUE", "AUTRE"});
    private final JTextField weightField = new JTextField("1");
    private final JButton addWeightedButton = new JButton("Ajouter produit pese");

    private final JComboBox<String> paymentModeCombo = new JComboBox<>(new String[]{"CASH", "CARD", "MOBILE", "VOUCHER"});
    private final JButton calculatorButton = new JButton("Calculatrice");
    private final JButton changePaymentButton = new JButton("Changer paiement");
    private final JButton finalizeButton = new JButton("Finaliser vente");
    private final JLabel ticketLabel = new JLabel("Ticket: -");
    private final JTextField ticketEmailField = new JTextField();
    private final JButton printTicketButton = new JButton("Imprimer ticket");
    private final JButton emailTicketButton = new JButton("Envoyer ticket par mail");

    private final JTextArea cartArea = new JTextArea();

    public POSView() {
        super("LeadelMarche - Point de Vente");
        setSize(1000, 1000);
        setLocationRelativeTo(null);
        Branding.applyWindowIcon(this);

        JPanel startPanel = new JPanel(new GridLayout(0, 2, 6, 6));
        startPanel.add(new JLabel("Point de vente"));
        startPanel.add(posIdField);
        startPanel.add(new JLabel("Badge caissier"));
        startPanel.add(badgeField);
        startPanel.add(new JLabel("Carte client (optionnel)"));
        startPanel.add(customerCardField);
        startPanel.add(new JLabel(""));
        startPanel.add(startSaleButton);
        startPanel.add(new JLabel(""));
        startPanel.add(saleIdLabel);

        JPanel linePanel = new JPanel(new GridLayout(0, 2, 6, 6));
        linePanel.add(new JLabel("Code-barres"));
        linePanel.add(barcodeField);
        linePanel.add(new JLabel("Quantite"));
        linePanel.add(qtyField);
        linePanel.add(new JLabel(""));
        linePanel.add(addBarcodeButton);
        linePanel.add(new JLabel("Type produit pese"));
        linePanel.add(weightedTypeCombo);
        linePanel.add(new JLabel("Poids (kg)"));
        linePanel.add(weightField);
        linePanel.add(new JLabel(""));
        linePanel.add(addWeightedButton);

        JPanel paymentPanel = new JPanel(new GridLayout(1, 4, 6, 6));
        paymentPanel.add(paymentModeCombo);
        paymentPanel.add(calculatorButton);
        paymentPanel.add(changePaymentButton);
        paymentPanel.add(finalizeButton);

        JPanel ticketPanel = new JPanel(new GridLayout(0, 2, 6, 6));
        ticketPanel.setBorder(BorderFactory.createTitledBorder("Ticket client"));
        ticketPanel.add(new JLabel("Dernier ticket"));
        ticketPanel.add(ticketLabel);
        ticketPanel.add(new JLabel("Email ticket"));
        ticketPanel.add(ticketEmailField);
        ticketPanel.add(printTicketButton);
        ticketPanel.add(emailTicketButton);

        JPanel north = new JPanel(new BorderLayout());
        north.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        north.add(startPanel, BorderLayout.NORTH);
        north.add(linePanel, BorderLayout.CENTER);
        JPanel actions = new JPanel(new GridLayout(2, 1, 6, 6));
        actions.add(paymentPanel);
        actions.add(ticketPanel);
        north.add(actions, BorderLayout.SOUTH);

        cartArea.setEditable(false);

        JPanel content = new JPanel(new BorderLayout(0, 6));
        content.add(north, BorderLayout.NORTH);
        content.add(new JScrollPane(cartArea), BorderLayout.CENTER);

        add(Branding.createHeader("Module Point de Vente"), BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
    }

    public JTextField posIdField() { return posIdField; }
    public JTextField badgeField() { return badgeField; }
    public JTextField customerCardField() { return customerCardField; }
    public JButton startSaleButton() { return startSaleButton; }
    public JLabel saleIdLabel() { return saleIdLabel; }
    public JTextField barcodeField() { return barcodeField; }
    public JTextField qtyField() { return qtyField; }
    public JButton addBarcodeButton() { return addBarcodeButton; }
    public JComboBox<String> weightedTypeCombo() { return weightedTypeCombo; }
    public JTextField weightField() { return weightField; }
    public JButton addWeightedButton() { return addWeightedButton; }
    public JComboBox<String> paymentModeCombo() { return paymentModeCombo; }
    public JButton calculatorButton() { return calculatorButton; }
    public JButton changePaymentButton() { return changePaymentButton; }
    public JButton finalizeButton() { return finalizeButton; }
    public JLabel ticketLabel() { return ticketLabel; }
    public JTextField ticketEmailField() { return ticketEmailField; }
    public JButton printTicketButton() { return printTicketButton; }
    public JButton emailTicketButton() { return emailTicketButton; }
    public JTextArea cartArea() { return cartArea; }

    public void setBadgePrefill(String badge, boolean editable) {
        badgeField.setText(badge == null ? "" : badge);
        badgeField.setEditable(editable);
    }
}
