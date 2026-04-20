package com.leadelmarche.ui.mvc.view;

import com.leadelmarche.domain.people.ContractType;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
    private final JComboBox<ContractType> contractTypeCombo = new JComboBox<>(ContractType.values());
    private final JTextField contractHoursField = new JTextField("35");
    private final JTextField editEmployeeIdField = new JTextField();
    private final JButton addButton = new JButton("Ajouter Employe");
    private final JButton updateButton = new JButton("Mettre a jour Employe");
    private final JButton deactivateButton = new JButton("Desactiver Employe");

    private final JTextField searchField = new JTextField();
    private final JButton searchButton = new JButton("Rechercher");
    private final JButton refreshButton = new JButton("Rafraichir");

    private final JTextField absenceBadgeField = new JTextField();
    private final JTextField absenceDateField = new JTextField(LocalDate.now().toString());
    private final JComboBox<String> absenceTypeCombo = new JComboBox<>(new String[]{"CONGE", "RTT", "MALADIE", "AUTRE"});
    private final JTextField absenceNoteField = new JTextField();
    private final JButton addAbsenceButton = new JButton("Saisir absence");

    private final JTextField weekStartField =
        new JTextField(LocalDate.now().with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)).toString());
    private final JTextField minStaffField = new JTextField("3");
    private final JButton generateScheduleButton = new JButton("Generer planning");
    private final JTextField exportBadgeField = new JTextField();
    private final JButton exportAllPdfButton = new JButton("Exporter PDF (tous)");
    private final JButton exportSelectedPdfButton = new JButton("Exporter PDF (badge)");
    private final JTextField forecastMonthsField = new JTextField("3");
    private final JButton forecastButton = new JButton("Verifier alertes effectif");

    private final JTextArea outputArea = new JTextArea();

    public StaffView() {
        super("LeadelMarche - Personnel");
        setSize(1200, 980);
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
        form.add(new JLabel("Type contrat"));
        form.add(contractTypeCombo);
        form.add(new JLabel("Heures contrat / semaine"));
        form.add(contractHoursField);
        form.add(new JLabel("ID employe (Update/Delete)"));
        form.add(editEmployeeIdField);
        form.add(updateButton);
        form.add(deactivateButton);
        form.add(new JLabel(" "));
        form.add(addButton);

        JPanel search = new JPanel(new GridLayout(1, 3, 6, 6));
        search.add(searchField);
        search.add(searchButton);
        search.add(refreshButton);

        JPanel absencesPanel = new JPanel(new GridLayout(0, 5, 6, 6));
        absencesPanel.setBorder(BorderFactory.createTitledBorder("Conges / RTT / Absences"));
        absencesPanel.add(new JLabel("Badge"));
        absencesPanel.add(new JLabel("Date (YYYY-MM-DD)"));
        absencesPanel.add(new JLabel("Type"));
        absencesPanel.add(new JLabel("Note"));
        absencesPanel.add(new JLabel(""));
        absencesPanel.add(absenceBadgeField);
        absencesPanel.add(absenceDateField);
        absencesPanel.add(absenceTypeCombo);
        absencesPanel.add(absenceNoteField);
        absencesPanel.add(addAbsenceButton);

        JPanel schedulePanel = new JPanel(new GridLayout(0, 4, 6, 6));
        schedulePanel.setBorder(BorderFactory.createTitledBorder("Generation planning & export PDF"));
        schedulePanel.add(new JLabel("Semaine (lundi)"));
        schedulePanel.add(weekStartField);
        schedulePanel.add(new JLabel("Effectif minimum/jour"));
        schedulePanel.add(minStaffField);
        schedulePanel.add(new JLabel(""));
        schedulePanel.add(generateScheduleButton);
        schedulePanel.add(new JLabel("Badge export (optionnel)"));
        schedulePanel.add(exportBadgeField);
        schedulePanel.add(exportAllPdfButton);
        schedulePanel.add(exportSelectedPdfButton);
        schedulePanel.add(new JLabel("Alerte mois avance"));
        schedulePanel.add(forecastMonthsField);
        schedulePanel.add(new JLabel(""));
        schedulePanel.add(forecastButton);
        schedulePanel.add(new JLabel(""));
        schedulePanel.add(new JLabel(""));

        JPanel north = new JPanel(new BorderLayout());
        north.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        north.add(form, BorderLayout.NORTH);
        north.add(search, BorderLayout.CENTER);

        JPanel planningSection = new JPanel(new BorderLayout(0, 6));
        planningSection.setBorder(BorderFactory.createEmptyBorder(0, 10, 8, 10));
        planningSection.add(absencesPanel, BorderLayout.NORTH);
        planningSection.add(schedulePanel, BorderLayout.CENTER);

        outputArea.setEditable(false);

        JPanel content = new JPanel(new BorderLayout(0, 6));
        content.add(north, BorderLayout.NORTH);
        JPanel centerBlock = new JPanel(new BorderLayout(0, 6));
        centerBlock.add(planningSection, BorderLayout.NORTH);
        centerBlock.add(new JScrollPane(outputArea), BorderLayout.CENTER);
        content.add(centerBlock, BorderLayout.CENTER);

        add(Branding.createHeader("Module Personnel & Horaires"), BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
    }

    public JTextField badgeField() { return badgeField; }
    public JTextField firstNameField() { return firstNameField; }
    public JTextField lastNameField() { return lastNameField; }
    public JTextField roleField() { return roleField; }
    public JTextField supervisorField() { return supervisorField; }
    public JComboBox<ContractType> contractTypeCombo() { return contractTypeCombo; }
    public JTextField contractHoursField() { return contractHoursField; }
    public JTextField editEmployeeIdField() { return editEmployeeIdField; }
    public JButton addButton() { return addButton; }
    public JButton updateButton() { return updateButton; }
    public JButton deactivateButton() { return deactivateButton; }
    public JTextField searchField() { return searchField; }
    public JButton searchButton() { return searchButton; }
    public JButton refreshButton() { return refreshButton; }
    public JTextField absenceBadgeField() { return absenceBadgeField; }
    public JTextField absenceDateField() { return absenceDateField; }
    public JComboBox<String> absenceTypeCombo() { return absenceTypeCombo; }
    public JTextField absenceNoteField() { return absenceNoteField; }
    public JButton addAbsenceButton() { return addAbsenceButton; }
    public JTextField weekStartField() { return weekStartField; }
    public JTextField minStaffField() { return minStaffField; }
    public JButton generateScheduleButton() { return generateScheduleButton; }
    public JTextField exportBadgeField() { return exportBadgeField; }
    public JButton exportAllPdfButton() { return exportAllPdfButton; }
    public JButton exportSelectedPdfButton() { return exportSelectedPdfButton; }
    public JTextField forecastMonthsField() { return forecastMonthsField; }
    public JButton forecastButton() { return forecastButton; }
    public JTextArea outputArea() { return outputArea; }
}
