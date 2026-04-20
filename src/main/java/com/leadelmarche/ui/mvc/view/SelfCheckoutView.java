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

public class SelfCheckoutView extends JFrame {
    private final JTextField posIdField = new JTextField("AUTO-001");
    private final JTextField customerCardField = new JTextField();
    private final JButton startSaleButton = new JButton("Demarrer caisse automatique");
    private final JLabel saleIdLabel = new JLabel("Sale ID: -");

    private final JTextField barcodeField = new JTextField();
    private final JTextField qtyField = new JTextField("1");
    private final JTextField measuredWeightField = new JTextField("1");
    private final JButton addScanButton = new JButton("Scanner et valider");

    private final JComboBox<Object> pieceProductCombo = new JComboBox<>();
    private final JTextField pieceQtyField = new JTextField("1");
    private final JButton addPieceButton = new JButton("Ajouter produit a la piece");

    private final JComboBox<Object> weightedProductCombo = new JComboBox<>();
    private final JTextField manualWeightField = new JTextField("1");
    private final JButton addWeightedButton = new JButton("Ajouter produit au poids");

    private final JComboBox<String> paymentModeCombo = new JComboBox<>(new String[]{"CARD", "MOBILE", "CASH", "VOUCHER"});
    private final JButton calculatorButton = new JButton("Calculatrice");
    private final JButton finalizeButton = new JButton("Finaliser paiement");
    private final JButton helpButton = new JButton("Appeler un caissier");
    private final JLabel ticketLabel = new JLabel("Ticket: -");
    private final JTextField ticketEmailField = new JTextField();
    private final JButton printTicketButton = new JButton("Imprimer ticket");
    private final JButton emailTicketButton = new JButton("Envoyer ticket par mail");

    private final JTextArea cartArea = new JTextArea();

    public SelfCheckoutView() {
        super("LeadelMarche - Caisse Automatique");
        setSize(1050, 900);
        setLocationRelativeTo(null);
        Branding.applyWindowIcon(this);

        JPanel startPanel = new JPanel(new GridLayout(0, 2, 6, 6));
        startPanel.add(new JLabel("ID Caisse"));
        startPanel.add(posIdField);
        startPanel.add(new JLabel("Carte fidelite (optionnel)"));
        startPanel.add(customerCardField);
        startPanel.add(new JLabel(""));
        startPanel.add(startSaleButton);
        startPanel.add(new JLabel(""));
        startPanel.add(saleIdLabel);

        JPanel scanPanel = new JPanel(new GridLayout(0, 2, 6, 6));
        scanPanel.setBorder(BorderFactory.createTitledBorder("Scan code-barres + controle poids balance"));
        scanPanel.add(new JLabel("Code-barres"));
        scanPanel.add(barcodeField);
        scanPanel.add(new JLabel("Quantite"));
        scanPanel.add(qtyField);
        scanPanel.add(new JLabel("Poids mesure (kg)"));
        scanPanel.add(measuredWeightField);
        scanPanel.add(new JLabel(""));
        scanPanel.add(addScanButton);

        JPanel piecePanel = new JPanel(new GridLayout(0, 2, 6, 6));
        piecePanel.setBorder(BorderFactory.createTitledBorder("Produits a la piece (sans code-barres)"));
        piecePanel.add(new JLabel("Produit"));
        piecePanel.add(pieceProductCombo);
        piecePanel.add(new JLabel("Quantite"));
        piecePanel.add(pieceQtyField);
        piecePanel.add(new JLabel(""));
        piecePanel.add(addPieceButton);

        JPanel weightedPanel = new JPanel(new GridLayout(0, 2, 6, 6));
        weightedPanel.setBorder(BorderFactory.createTitledBorder("Produits au poids (selection manuelle)"));
        weightedPanel.add(new JLabel("Produit"));
        weightedPanel.add(weightedProductCombo);
        weightedPanel.add(new JLabel("Poids (kg)"));
        weightedPanel.add(manualWeightField);
        weightedPanel.add(new JLabel(""));
        weightedPanel.add(addWeightedButton);

        JPanel paymentPanel = new JPanel(new GridLayout(1, 4, 6, 6));
        paymentPanel.add(paymentModeCombo);
        paymentPanel.add(calculatorButton);
        paymentPanel.add(helpButton);
        paymentPanel.add(finalizeButton);

        JPanel ticketPanel = new JPanel(new GridLayout(0, 2, 6, 6));
        ticketPanel.setBorder(BorderFactory.createTitledBorder("Ticket client"));
        ticketPanel.add(new JLabel("Dernier ticket"));
        ticketPanel.add(ticketLabel);
        ticketPanel.add(new JLabel("Email ticket"));
        ticketPanel.add(ticketEmailField);
        ticketPanel.add(printTicketButton);
        ticketPanel.add(emailTicketButton);

        JPanel top = new JPanel(new BorderLayout(0, 6));
        top.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        top.add(startPanel, BorderLayout.NORTH);

        JPanel centerInput = new JPanel(new GridLayout(1, 3, 6, 6));
        centerInput.add(scanPanel);
        centerInput.add(piecePanel);
        centerInput.add(weightedPanel);
        top.add(centerInput, BorderLayout.CENTER);
        JPanel actionPanel = new JPanel(new GridLayout(2, 1, 6, 6));
        actionPanel.add(paymentPanel);
        actionPanel.add(ticketPanel);
        top.add(actionPanel, BorderLayout.SOUTH);

        cartArea.setEditable(false);

        JPanel content = new JPanel(new BorderLayout(0, 6));
        content.add(top, BorderLayout.NORTH);
        content.add(new JScrollPane(cartArea), BorderLayout.CENTER);

        add(Branding.createHeader("Module Caisse Automatique"), BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
    }

    public JTextField posIdField() { return posIdField; }
    public JTextField customerCardField() { return customerCardField; }
    public JButton startSaleButton() { return startSaleButton; }
    public JLabel saleIdLabel() { return saleIdLabel; }
    public JTextField barcodeField() { return barcodeField; }
    public JTextField qtyField() { return qtyField; }
    public JTextField measuredWeightField() { return measuredWeightField; }
    public JButton addScanButton() { return addScanButton; }
    public JComboBox<Object> pieceProductCombo() { return pieceProductCombo; }
    public JTextField pieceQtyField() { return pieceQtyField; }
    public JButton addPieceButton() { return addPieceButton; }
    public JComboBox<Object> weightedProductCombo() { return weightedProductCombo; }
    public JTextField manualWeightField() { return manualWeightField; }
    public JButton addWeightedButton() { return addWeightedButton; }
    public JComboBox<String> paymentModeCombo() { return paymentModeCombo; }
    public JButton calculatorButton() { return calculatorButton; }
    public JButton finalizeButton() { return finalizeButton; }
    public JButton helpButton() { return helpButton; }
    public JLabel ticketLabel() { return ticketLabel; }
    public JTextField ticketEmailField() { return ticketEmailField; }
    public JButton printTicketButton() { return printTicketButton; }
    public JButton emailTicketButton() { return emailTicketButton; }
    public JTextArea cartArea() { return cartArea; }
}
