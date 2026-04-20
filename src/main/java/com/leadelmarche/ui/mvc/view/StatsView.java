package com.leadelmarche.ui.mvc.view;

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
import javax.swing.JTextField;
import javax.swing.JTextArea;
import com.leadelmarche.service.StatisticsService;

public class StatsView extends JFrame {
    private final JComboBox<StatisticsService.StatisticOption> statisticCombo = new JComboBox<>();
    private final JComboBox<String> displayModeCombo = new JComboBox<>(new String[]{"TABLEAU", "BARRES", "CAMEMBERT"});
    private final JTextField periodStartField = new JTextField(LocalDate.now().minusDays(30).toString());
    private final JTextField periodEndField = new JTextField(LocalDate.now().toString());
    private final JCheckBox compareCheckBox = new JCheckBox("Comparer avec une seconde periode");
    private final JTextField compareStartField = new JTextField(LocalDate.now().minusDays(60).toString());
    private final JTextField compareEndField = new JTextField(LocalDate.now().minusDays(31).toString());
    private final JButton computeButton = new JButton("Calculer statistique");
    private final JTextField customStatNameField = new JTextField();
    private final JTextField customProductFilterField = new JTextField();
    private final JButton addCustomStatButton = new JButton("Ajouter statistique produit");
    private final JTextField absenceBadgeField = new JTextField();
    private final JTextField absenceDateField = new JTextField(LocalDate.now().toString());
    private final JComboBox<String> absenceTypeCombo = new JComboBox<>(new String[]{"CONGE", "RTT", "MALADIE", "AUTRE"});
    private final JTextField absenceNoteField = new JTextField();
    private final JButton addAbsenceButton = new JButton("Saisir absence");
    private final JTextArea outputArea = new JTextArea();

    public StatsView() {
        super("LeadelMarche - Statistiques");
        setSize(980, 700);
        setLocationRelativeTo(null);

        JPanel analysisPanel = new JPanel(new GridLayout(3, 4, 8, 8));
        analysisPanel.setBorder(BorderFactory.createTitledBorder("Analyse"));
        analysisPanel.add(new JLabel("Statistique"));
        analysisPanel.add(statisticCombo);
        analysisPanel.add(new JLabel("Affichage"));
        analysisPanel.add(displayModeCombo);
        analysisPanel.add(new JLabel("Debut periode A (YYYY-MM-DD)"));
        analysisPanel.add(periodStartField);
        analysisPanel.add(new JLabel("Fin periode A (YYYY-MM-DD)"));
        analysisPanel.add(periodEndField);
        analysisPanel.add(compareCheckBox);
        analysisPanel.add(new JLabel(""));
        analysisPanel.add(new JLabel(""));
        analysisPanel.add(computeButton);

        JPanel comparePanel = new JPanel(new GridLayout(1, 4, 8, 8));
        comparePanel.setBorder(BorderFactory.createTitledBorder("Periode B"));
        comparePanel.add(new JLabel("Debut (YYYY-MM-DD)"));
        comparePanel.add(compareStartField);
        comparePanel.add(new JLabel("Fin (YYYY-MM-DD)"));
        comparePanel.add(compareEndField);

        JPanel customStatPanel = new JPanel(new GridLayout(2, 3, 8, 8));
        customStatPanel.setBorder(BorderFactory.createTitledBorder("Creer une nouvelle statistique"));
        customStatPanel.add(new JLabel("Nom statistique"));
        customStatPanel.add(new JLabel("Produit (nom partiel)"));
        customStatPanel.add(new JLabel(""));
        customStatPanel.add(customStatNameField);
        customStatPanel.add(customProductFilterField);
        customStatPanel.add(addCustomStatButton);

        JPanel absencePanel = new JPanel(new GridLayout(2, 5, 8, 8));
        absencePanel.setBorder(BorderFactory.createTitledBorder("Saisie absences personnel"));
        absencePanel.add(new JLabel("Badge"));
        absencePanel.add(new JLabel("Date (YYYY-MM-DD)"));
        absencePanel.add(new JLabel("Type"));
        absencePanel.add(new JLabel("Note"));
        absencePanel.add(new JLabel(""));
        absencePanel.add(absenceBadgeField);
        absencePanel.add(absenceDateField);
        absencePanel.add(absenceTypeCombo);
        absencePanel.add(absenceNoteField);
        absencePanel.add(addAbsenceButton);

        JPanel topPanel = new JPanel(new GridLayout(4, 1, 8, 8));
        topPanel.add(analysisPanel);
        topPanel.add(comparePanel);
        topPanel.add(customStatPanel);
        topPanel.add(absencePanel);

        periodStartField.setToolTipText("Format attendu: YYYY-MM-DD");
        periodEndField.setToolTipText("Format attendu: YYYY-MM-DD");
        compareStartField.setToolTipText("Format attendu: YYYY-MM-DD");
        compareEndField.setToolTipText("Format attendu: YYYY-MM-DD");
        customProductFilterField.setToolTipText("Exemple: pomme");
        absenceDateField.setToolTipText("Format attendu: YYYY-MM-DD");

        outputArea.setEditable(false);
        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(outputArea), BorderLayout.CENTER);
    }

    public JComboBox<StatisticsService.StatisticOption> statisticCombo() {
        return statisticCombo;
    }

    public JComboBox<String> displayModeCombo() {
        return displayModeCombo;
    }

    public JTextField periodStartField() {
        return periodStartField;
    }

    public JTextField periodEndField() {
        return periodEndField;
    }

    public JCheckBox compareCheckBox() {
        return compareCheckBox;
    }

    public JTextField compareStartField() {
        return compareStartField;
    }

    public JTextField compareEndField() {
        return compareEndField;
    }

    public JButton computeButton() {
        return computeButton;
    }

    public JTextField customStatNameField() {
        return customStatNameField;
    }

    public JTextField customProductFilterField() {
        return customProductFilterField;
    }

    public JButton addCustomStatButton() {
        return addCustomStatButton;
    }

    public JTextField absenceBadgeField() {
        return absenceBadgeField;
    }

    public JTextField absenceDateField() {
        return absenceDateField;
    }

    public JComboBox<String> absenceTypeCombo() {
        return absenceTypeCombo;
    }

    public JTextField absenceNoteField() {
        return absenceNoteField;
    }

    public JButton addAbsenceButton() {
        return addAbsenceButton;
    }

    public JTextArea outputArea() {
        return outputArea;
    }

    public void setComparisonFieldsEnabled(boolean enabled) {
        compareStartField.setEnabled(enabled);
        compareEndField.setEnabled(enabled);
    }
}
