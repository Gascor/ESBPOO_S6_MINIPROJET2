package com.leadelmarche.ui.mvc.view;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class StockView extends JFrame {
    private final JTextField skuField = new JTextField();
    private final JTextField nameField = new JTextField();
    private final JTextField barcodeField = new JTextField();
    private final JComboBox<String> typeCombo = new JComboBox<>(new String[]{"ALIMENTAIRE", "MEDECINE", "ELECTRONIQUE", "AUTRE"});
    private final JTextField priceField = new JTextField("0.00");
    private final JTextField vatField = new JTextField("20");
    private final JTextField initStockField = new JTextField("0");
    private final JCheckBox weightedBox = new JCheckBox("Produit pese");
    private final JCheckBox pieceNoBarcodeBox = new JCheckBox("Piece sans code-barres");
    private final JButton addProductButton = new JButton("Ajouter Produit");

    private final JTextField restockProductIdField = new JTextField();
    private final JTextField restockQtyField = new JTextField("1");
    private final JButton restockButton = new JButton("Recharger Stock");

    private final JTextField searchField = new JTextField();
    private final JButton searchButton = new JButton("Rechercher");
    private final JButton refreshButton = new JButton("Rafraichir");
    private final JTextArea outputArea = new JTextArea();

    public StockView() {
        super("LeadelMarche - Stock");
        setSize(1000, 1000);
        setLocationRelativeTo(null);
        Branding.applyWindowIcon(this);

        JPanel addForm = new JPanel(new GridLayout(0, 2, 6, 6));
        addForm.add(new JLabel("SKU"));
        addForm.add(skuField);
        addForm.add(new JLabel("Nom"));
        addForm.add(nameField);
        addForm.add(new JLabel("Code-barres"));
        addForm.add(barcodeField);
        addForm.add(new JLabel("Type"));
        addForm.add(typeCombo);
        addForm.add(new JLabel("Prix HT"));
        addForm.add(priceField);
        addForm.add(new JLabel("TVA %"));
        addForm.add(vatField);
        addForm.add(new JLabel("Stock initial"));
        addForm.add(initStockField);
        addForm.add(weightedBox);
        addForm.add(pieceNoBarcodeBox);
        addForm.add(new JLabel(""));
        addForm.add(addProductButton);

        JPanel restockForm = new JPanel(new GridLayout(0, 2, 6, 6));
        restockForm.add(new JLabel("Product ID"));
        restockForm.add(restockProductIdField);
        restockForm.add(new JLabel("Quantite"));
        restockForm.add(restockQtyField);
        restockForm.add(new JLabel(""));
        restockForm.add(restockButton);

        JPanel searchPanel = new JPanel(new GridLayout(1, 3, 6, 6));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(refreshButton);

        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        top.add(addForm, BorderLayout.NORTH);
        top.add(restockForm, BorderLayout.CENTER);
        top.add(searchPanel, BorderLayout.SOUTH);

        outputArea.setEditable(false);
        outputArea.setLineWrap(false);

        JPanel content = new JPanel(new BorderLayout(0, 6));
        content.add(top, BorderLayout.NORTH);
        content.add(new JScrollPane(outputArea), BorderLayout.CENTER);

        add(Branding.createHeader("Module Stock"), BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
    }

    public JTextField skuField() { return skuField; }
    public JTextField nameField() { return nameField; }
    public JTextField barcodeField() { return barcodeField; }
    public JComboBox<String> typeCombo() { return typeCombo; }
    public JTextField priceField() { return priceField; }
    public JTextField vatField() { return vatField; }
    public JTextField initStockField() { return initStockField; }
    public JCheckBox weightedBox() { return weightedBox; }
    public JCheckBox pieceNoBarcodeBox() { return pieceNoBarcodeBox; }
    public JButton addProductButton() { return addProductButton; }
    public JTextField restockProductIdField() { return restockProductIdField; }
    public JTextField restockQtyField() { return restockQtyField; }
    public JButton restockButton() { return restockButton; }
    public JTextField searchField() { return searchField; }
    public JButton searchButton() { return searchButton; }
    public JButton refreshButton() { return refreshButton; }
    public JTextArea outputArea() { return outputArea; }
}
