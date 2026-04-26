package com.leadelmarche.ui.mvc.view;

import com.leadelmarche.domain.promotion.PromotionEffect;
import com.leadelmarche.domain.promotion.PromotionRuleType;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
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

public class PromotionView extends JFrame {
    private final JTextField nameField = new JTextField();
    private final JTextField productIdField = new JTextField();
    private final JTextField startField = new JTextField(LocalDate.now().toString());
    private final JTextField endField = new JTextField(LocalDate.now().plusDays(30).toString());
    private final JCheckBox renewableBox = new JCheckBox("Promotion renouvelable");
    private final JComboBox<PromotionRuleType> typeCombo = new JComboBox<>(PromotionRuleType.values());
    private final JComboBox<PromotionEffect> effectCombo = new JComboBox<>(PromotionEffect.values());
    private final JTextField buyQtyField = new JTextField("2");
    private final JTextField freeQtyField = new JTextField("1");
    private final JTextField nthItemField = new JTextField("2");
    private final JTextField percentField = new JTextField("30");
    private final JButton addButton = new JButton("Creer promotion");
    private final JTextField deactivateIdField = new JTextField();
    private final JButton deactivateButton = new JButton("Desactiver promotion");
    private final JButton refreshButton = new JButton("Rafraichir");
    private final JTextArea outputArea = new JTextArea();

    public PromotionView() {
        super("LeadelMarche - Promotions");
        setSize(980, 820);
        setLocationRelativeTo(null);
        Branding.applyWindowIcon(this);

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Nom"));
        form.add(nameField);
        form.add(new JLabel("Product ID (vide = tous)"));
        form.add(productIdField);
        form.add(new JLabel("Debut (YYYY-MM-DD)"));
        form.add(startField);
        form.add(new JLabel("Fin (YYYY-MM-DD)"));
        form.add(endField);
        form.add(new JLabel("Type"));
        form.add(typeCombo);
        form.add(new JLabel("Effet"));
        form.add(effectCombo);
        form.add(new JLabel("Achetes (Buy X)"));
        form.add(buyQtyField);
        form.add(new JLabel("Offerts (Get Y)"));
        form.add(freeQtyField);
        form.add(new JLabel("Nth item (ex: 2)"));
        form.add(nthItemField);
        form.add(new JLabel("% remise sur nth"));
        form.add(percentField);
        form.add(renewableBox);
        form.add(addButton);

        JPanel actions = new JPanel(new GridLayout(1, 3, 6, 6));
        actions.add(deactivateIdField);
        actions.add(deactivateButton);
        actions.add(refreshButton);

        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        top.add(form, BorderLayout.NORTH);
        top.add(actions, BorderLayout.SOUTH);

        outputArea.setEditable(false);

        JPanel content = new JPanel(new BorderLayout(0, 6));
        content.add(top, BorderLayout.NORTH);
        content.add(new JScrollPane(outputArea), BorderLayout.CENTER);

        add(
            Branding.createHeader(
                "Module Promotions",
                "Aide - Module Promotions",
                "1) Definir nom, periode, type et effet de promotion.\n"
                    + "2) Product ID vide = promotion globale.\n"
                    + "3) BUY_X_GET_Y: renseigner Achetes et Offerts.\n"
                    + "4) PERCENT_ON_NTH: renseigner nth item et % remise.\n"
                    + "5) Desactivation via ID promotion."
            ),
            BorderLayout.NORTH
        );
        add(content, BorderLayout.CENTER);
    }

    public JTextField nameField() { return nameField; }
    public JTextField productIdField() { return productIdField; }
    public JTextField startField() { return startField; }
    public JTextField endField() { return endField; }
    public JCheckBox renewableBox() { return renewableBox; }
    public JComboBox<PromotionRuleType> typeCombo() { return typeCombo; }
    public JComboBox<PromotionEffect> effectCombo() { return effectCombo; }
    public JTextField buyQtyField() { return buyQtyField; }
    public JTextField freeQtyField() { return freeQtyField; }
    public JTextField nthItemField() { return nthItemField; }
    public JTextField percentField() { return percentField; }
    public JButton addButton() { return addButton; }
    public JTextField deactivateIdField() { return deactivateIdField; }
    public JButton deactivateButton() { return deactivateButton; }
    public JButton refreshButton() { return refreshButton; }
    public JTextArea outputArea() { return outputArea; }
}
