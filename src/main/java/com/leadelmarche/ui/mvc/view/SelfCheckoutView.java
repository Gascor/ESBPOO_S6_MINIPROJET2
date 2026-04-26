package com.leadelmarche.ui.mvc.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class SelfCheckoutView extends JFrame {
    private final JTextField posIdField = new JTextField("AUTO-001");
    private final JTextField customerCardField = new JTextField();
    private final JButton startSaleButton = new JButton("Demarrer caisse automatique");
    private final JLabel saleIdLabel = new JLabel("Vente: -");
    private final JLabel sessionStatusLabel = new JLabel("Session: en attente");
    private final JLabel weightStatusLabel = new JLabel("Balance: en attente");
    private final JLabel assistanceStatusLabel = new JLabel("Assistance: aucune demande");
    private final JLabel totalLabel = new JLabel("Total TTC: 0.00");

    private final JTextField barcodeField = new JTextField();
    private final JTextField qtyField = new JTextField("1");
    private final JTextField measuredWeightField = new JTextField("1");
    private final JButton addScanButton = new JButton("Scanner + valider balance");

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
        setSize(1220, 920);
        setLocationRelativeTo(null);
        Branding.applyWindowIcon(this);

        cartArea.setEditable(false);
        cartArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        sessionStatusLabel.setForeground(new Color(90, 90, 90));
        weightStatusLabel.setForeground(new Color(90, 90, 90));
        assistanceStatusLabel.setForeground(new Color(90, 90, 90));
        totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD, 20f));
        totalLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel startPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        startPanel.setBorder(BorderFactory.createTitledBorder("Initialisation caisse"));
        startPanel.add(new JLabel("ID Caisse"));
        startPanel.add(posIdField);
        startPanel.add(new JLabel("Carte fidelite (optionnel)"));
        startPanel.add(customerCardField);
        startPanel.add(startSaleButton);
        startPanel.add(saleIdLabel);

        JPanel statusPanel = new JPanel(new GridLayout(0, 1, 6, 6));
        statusPanel.setBorder(BorderFactory.createTitledBorder("Statut caisse rapide"));
        statusPanel.add(sessionStatusLabel);
        statusPanel.add(weightStatusLabel);
        statusPanel.add(assistanceStatusLabel);

        JPanel scanPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        scanPanel.setBorder(BorderFactory.createTitledBorder("1) Scanner + controle balance"));
        scanPanel.add(new JLabel("Code-barres"));
        scanPanel.add(barcodeField);
        scanPanel.add(new JLabel("Quantite"));
        scanPanel.add(qtyField);
        scanPanel.add(new JLabel("Poids mesure (kg)"));
        scanPanel.add(measuredWeightField);
        scanPanel.add(new JLabel(""));
        scanPanel.add(addScanButton);

        JPanel piecePanel = new JPanel(new GridLayout(0, 2, 8, 8));
        piecePanel.setBorder(BorderFactory.createTitledBorder("2a) Sans code-barres (a la piece)"));
        piecePanel.add(new JLabel("Produit"));
        piecePanel.add(pieceProductCombo);
        piecePanel.add(new JLabel("Nombre de pieces"));
        piecePanel.add(pieceQtyField);
        piecePanel.add(new JLabel(""));
        piecePanel.add(addPieceButton);

        JPanel weightedPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        weightedPanel.setBorder(BorderFactory.createTitledBorder("2b) Sans scan (produit au poids)"));
        weightedPanel.add(new JLabel("Produit"));
        weightedPanel.add(weightedProductCombo);
        weightedPanel.add(new JLabel("Poids saisi (kg)"));
        weightedPanel.add(manualWeightField);
        weightedPanel.add(new JLabel(""));
        weightedPanel.add(addWeightedButton);

        JTabbedPane manualTabs = new JTabbedPane();
        manualTabs.addTab("A la piece", piecePanel);
        manualTabs.addTab("Au poids", weightedPanel);

        JPanel paymentPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        paymentPanel.setBorder(BorderFactory.createTitledBorder("3) Paiement & assistance"));
        paymentPanel.add(new JLabel("Mode paiement"));
        paymentPanel.add(paymentModeCombo);
        paymentPanel.add(calculatorButton);
        paymentPanel.add(finalizeButton);
        paymentPanel.add(helpButton);
        paymentPanel.add(new JLabel(""));

        JPanel leftColumn = new JPanel(new BorderLayout(0, 8));
        JPanel leftTop = new JPanel(new GridLayout(1, 2, 8, 8));
        leftTop.add(startPanel);
        leftTop.add(statusPanel);
        leftColumn.add(leftTop, BorderLayout.NORTH);
        JPanel leftCenter = new JPanel(new GridLayout(3, 1, 8, 8));
        leftCenter.add(scanPanel);
        leftCenter.add(manualTabs);
        leftCenter.add(paymentPanel);
        leftColumn.add(leftCenter, BorderLayout.CENTER);

        JPanel ticketPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        ticketPanel.setBorder(BorderFactory.createTitledBorder("Ticket client"));
        ticketPanel.add(new JLabel("Dernier ticket"));
        ticketPanel.add(ticketLabel);
        ticketPanel.add(new JLabel("Email ticket"));
        ticketPanel.add(ticketEmailField);
        ticketPanel.add(printTicketButton);
        ticketPanel.add(emailTicketButton);

        JPanel summaryPanel = new JPanel(new BorderLayout(0, 8));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Panier & resume"));
        summaryPanel.add(totalLabel, BorderLayout.NORTH);
        summaryPanel.add(new JScrollPane(cartArea), BorderLayout.CENTER);
        summaryPanel.add(ticketPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftColumn, summaryPanel);
        splitPane.setResizeWeight(0.54);
        splitPane.setDividerLocation(650);

        JPanel content = new JPanel(new BorderLayout(0, 8));
        content.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        content.add(splitPane, BorderLayout.CENTER);

        add(Branding.createHeader("Module Caisse Automatique"), BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
    }

    public void setSessionStatus(String text, Color color) {
        sessionStatusLabel.setText(text);
        sessionStatusLabel.setForeground(color == null ? new Color(90, 90, 90) : color);
    }

    public void setWeightStatus(String text, Color color) {
        weightStatusLabel.setText(text);
        weightStatusLabel.setForeground(color == null ? new Color(90, 90, 90) : color);
    }

    public void setAssistanceStatus(String text, Color color) {
        assistanceStatusLabel.setText(text);
        assistanceStatusLabel.setForeground(color == null ? new Color(90, 90, 90) : color);
    }

    public void setTotalDisplay(String text) {
        totalLabel.setText(text == null || text.isBlank() ? "Total TTC: 0.00" : text);
    }

    public void setCheckoutControlsEnabled(boolean enabled) {
        addScanButton.setEnabled(enabled);
        addPieceButton.setEnabled(enabled);
        addWeightedButton.setEnabled(enabled);
        paymentModeCombo.setEnabled(enabled);
        calculatorButton.setEnabled(enabled);
        finalizeButton.setEnabled(enabled);
        helpButton.setEnabled(enabled);
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
