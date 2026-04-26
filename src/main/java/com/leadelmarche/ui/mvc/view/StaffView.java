package com.leadelmarche.ui.mvc.view;

import com.leadelmarche.domain.people.ContractType;
import com.leadelmarche.domain.people.Employee;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.AbstractTableModel;

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
    private final JButton manageNeedsButton = new JButton("Gestion graphe besoin");
    private final JButton generateScheduleButton = new JButton("Generer planning optimise");
    private final JTextField exportBadgeField = new JTextField();
    private final JButton exportAllPdfButton = new JButton("Exporter PDF (tous)");
    private final JButton exportSelectedPdfButton = new JButton("Exporter PDF (badge)");
    private final JTextField forecastMonthsField = new JTextField("3");
    private final JButton forecastButton = new JButton("Verifier alertes effectif");

    private final JTextField planningYearField = new JTextField(String.valueOf(LocalDate.now().getYear()));
    private final JComboBox<Integer> planningWeekCombo = new JComboBox<>();
    private final JComboBox<String> planningEmployeeCombo = new JComboBox<>();
    private final JTextField planningContractHoursField = new JTextField();
    private final JTextField planningOvertimeHoursField = new JTextField("0");
    private final JButton loadPlanningButton = new JButton("Charger semaine");
    private final JButton saveManualPlanningButton = new JButton("Enregistrer horaires");
    private final JLabel planningStatusLabel = new JLabel(" ");
    private final WeeklyScheduleTableModel planningTableModel = new WeeklyScheduleTableModel();
    private final WeeklyTimelinePanel timelinePanel = new WeeklyTimelinePanel();
    private final StaffingNeedsChart needsChart = new StaffingNeedsChart();

    private final JTextArea outputArea = new JTextArea();

    public StaffView() {
        super("LeadelMarche - Personnel");
        setSize(1320, 1200);
        setLocationRelativeTo(null);
        Branding.applyWindowIcon(this);

        planningContractHoursField.setEditable(false);
        planningContractHoursField.setBackground(new Color(232, 232, 232));
        planningContractHoursField.setForeground(new Color(80, 80, 80));

        outputArea.setEditable(false);
        outputArea.setRows(7);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Personnel", buildEmployeeTab());
        tabs.addTab("Planning semaine", buildPlanningTab());

        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(BorderFactory.createTitledBorder("Journal / resultats"));
        logPanel.add(new JScrollPane(outputArea), BorderLayout.CENTER);

        JPanel content = new JPanel(new BorderLayout(0, 8));
        content.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        content.add(tabs, BorderLayout.CENTER);
        content.add(logPanel, BorderLayout.SOUTH);

        add(
            Branding.createHeader(
                "Module Personnel & Horaires",
                "Aide - Personnel et planning",
                "1) Onglet Personnel: CRUD employes + contrat.\n"
                    + "2) Planning semaine: absences, generation, saisie manuelle.\n"
                    + "3) Frise: glisser les barres pour ajuster debut/fin.\n"
                    + "4) Clic droit sur une ligne pour ajouter/supprimer un horaire.\n"
                    + "5) Besoins: graphe dynamique + bouton Gestion graphe besoin.\n"
                    + "6) Enregistrer horaires seulement quand les lignes ne sont pas rouges."
            ),
            BorderLayout.NORTH
        );
        add(content, BorderLayout.CENTER);
    }

    private JPanel buildEmployeeTab() {
        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.setBorder(BorderFactory.createTitledBorder("Fiche employe"));
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
        search.setBorder(BorderFactory.createTitledBorder("Recherche personnel"));
        search.add(searchField);
        search.add(searchButton);
        search.add(refreshButton);

        JPanel tab = new JPanel(new BorderLayout(0, 8));
        tab.add(form, BorderLayout.NORTH);
        tab.add(search, BorderLayout.CENTER);
        return tab;
    }

    private JPanel buildPlanningTab() {
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

        JPanel schedulePanel = new JPanel(new GridLayout(0, 5, 6, 6));
        schedulePanel.setBorder(BorderFactory.createTitledBorder("Generation planning & export PDF"));
        schedulePanel.add(new JLabel("Semaine (lundi)"));
        schedulePanel.add(weekStartField);
        schedulePanel.add(new JLabel("Effectif minimum/heure"));
        schedulePanel.add(minStaffField);
        schedulePanel.add(manageNeedsButton);
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

        JPanel manualPanel = new JPanel(new GridLayout(0, 6, 6, 6));
        manualPanel.setBorder(BorderFactory.createTitledBorder("Saisie manuelle par employe et semaine ISO"));
        manualPanel.add(new JLabel("Annee"));
        manualPanel.add(planningYearField);
        manualPanel.add(new JLabel("Semaine"));
        manualPanel.add(planningWeekCombo);
        manualPanel.add(new JLabel("Employe"));
        manualPanel.add(planningEmployeeCombo);
        manualPanel.add(new JLabel("Heures contrat"));
        manualPanel.add(planningContractHoursField);
        manualPanel.add(new JLabel("Heures supp semaine"));
        manualPanel.add(planningOvertimeHoursField);
        manualPanel.add(loadPlanningButton);
        manualPanel.add(saveManualPlanningButton);

        JPanel top = new JPanel(new BorderLayout(0, 6));
        top.add(absencesPanel, BorderLayout.NORTH);
        top.add(schedulePanel, BorderLayout.CENTER);
        top.add(manualPanel, BorderLayout.SOUTH);

        timelinePanel.setBorder(BorderFactory.createTitledBorder("Frise horaire 08:00-20:00"));
        timelinePanel.setPreferredSize(new Dimension(520, 230));
        needsChart.setBorder(BorderFactory.createTitledBorder("Besoins estimes selon affluence"));
        needsChart.setPreferredSize(new Dimension(520, 230));

        JPanel timelineBlock = new JPanel(new BorderLayout(0, 6));
        timelineBlock.add(timelinePanel, BorderLayout.CENTER);
        timelineBlock.add(planningStatusLabel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, timelineBlock, needsChart);
        splitPane.setResizeWeight(0.72);

        JPanel tab = new JPanel(new BorderLayout(0, 8));
        tab.add(top, BorderLayout.NORTH);
        tab.add(splitPane, BorderLayout.CENTER);
        return tab;
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
    public JButton manageNeedsButton() { return manageNeedsButton; }
    public JButton generateScheduleButton() { return generateScheduleButton; }
    public JTextField exportBadgeField() { return exportBadgeField; }
    public JButton exportAllPdfButton() { return exportAllPdfButton; }
    public JButton exportSelectedPdfButton() { return exportSelectedPdfButton; }
    public JTextField forecastMonthsField() { return forecastMonthsField; }
    public JButton forecastButton() { return forecastButton; }
    public JTextArea outputArea() { return outputArea; }
    public JTextField planningYearField() { return planningYearField; }
    public JComboBox<Integer> planningWeekCombo() { return planningWeekCombo; }
    public JComboBox<String> planningEmployeeCombo() { return planningEmployeeCombo; }
    public JTextField planningContractHoursField() { return planningContractHoursField; }
    public JTextField planningOvertimeHoursField() { return planningOvertimeHoursField; }
    public JButton loadPlanningButton() { return loadPlanningButton; }
    public JButton saveManualPlanningButton() { return saveManualPlanningButton; }
    public WeeklyScheduleTableModel planningTableModel() { return planningTableModel; }
    public WeeklyTimelinePanel timelinePanel() { return timelinePanel; }
    public StaffingNeedsChart needsChart() { return needsChart; }
    public JLabel planningStatusLabel() { return planningStatusLabel; }

    public void setTimelineActionHandler(TimelineActionHandler handler) {
        timelinePanel.setActionHandler(handler);
    }

    public void showNeedEditor(List<NeedEditorSlot> slots, int maxValue, NeedEditorApplyHandler applyHandler) {
        JDialog dialog = new JDialog(this, "Gestion graphe besoin", true);
        dialog.setSize(1120, 620);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(8, 8));

        JSpinner maxSpinner = new JSpinner(new SpinnerNumberModel(Math.max(1, maxValue), 1, 99, 1));
        JPanel header = new JPanel(new GridLayout(1, 4, 6, 6));
        header.setBorder(BorderFactory.createEmptyBorder(8, 10, 0, 10));
        header.add(new JLabel("Maximum besoin"));
        header.add(maxSpinner);
        header.add(new JLabel("Tranches de 15 minutes"));
        header.add(new JLabel("Glisser les barres verticales pour ajuster"));

        Map<LocalDate, List<NeedEditorSlot>> slotsByDate = new LinkedHashMap<>();
        for (NeedEditorSlot slot : slots) {
            slotsByDate.computeIfAbsent(slot.date, key -> new ArrayList<>()).add(slot.copy());
        }

        JTabbedPane tabs = new JTabbedPane();
        List<NeedDayEditorPanel> panels = new ArrayList<>();
        for (Map.Entry<LocalDate, List<NeedEditorSlot>> entry : slotsByDate.entrySet()) {
            NeedDayEditorPanel panel = new NeedDayEditorPanel(entry.getValue(), (Integer) maxSpinner.getValue());
            panels.add(panel);
            tabs.addTab(dayShort(entry.getKey()) + " " + entry.getKey(), panel);
        }
        maxSpinner.addChangeListener(e -> {
            int newMax = (Integer) maxSpinner.getValue();
            for (NeedDayEditorPanel panel : panels) {
                panel.setMaxValue(newMax);
            }
        });

        JPanel footer = new JPanel(new GridLayout(1, 4, 6, 6));
        footer.setBorder(BorderFactory.createEmptyBorder(0, 10, 8, 10));
        JButton zeroButton = new JButton("Jour a 0");
        JButton baseButton = new JButton("Limiter au max");
        JButton cancelButton = new JButton("Annuler");
        JButton applyButton = new JButton("Appliquer");
        zeroButton.addActionListener(e -> {
            NeedDayEditorPanel panel = (NeedDayEditorPanel) tabs.getSelectedComponent();
            if (panel != null) {
                panel.setAllValues(0);
            }
        });
        baseButton.addActionListener(e -> {
            NeedDayEditorPanel panel = (NeedDayEditorPanel) tabs.getSelectedComponent();
            if (panel != null) {
                panel.clampValues();
            }
        });
        cancelButton.addActionListener(e -> dialog.dispose());
        applyButton.addActionListener(e -> {
            List<NeedEditorSlot> edited = new ArrayList<>();
            for (NeedDayEditorPanel panel : panels) {
                edited.addAll(panel.slots());
            }
            applyHandler.apply(edited, (Integer) maxSpinner.getValue());
            dialog.dispose();
        });
        footer.add(zeroButton);
        footer.add(baseButton);
        footer.add(cancelButton);
        footer.add(applyButton);

        dialog.add(header, BorderLayout.NORTH);
        dialog.add(tabs, BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    public void setPlanningWeeks(int weekCount, int selectedWeek) {
        planningWeekCombo.removeAllItems();
        int safeCount = Math.max(1, weekCount);
        for (int week = 1; week <= safeCount; week++) {
            planningWeekCombo.addItem(week);
        }
        planningWeekCombo.setSelectedItem(Math.max(1, Math.min(selectedWeek, safeCount)));
    }

    public void setPlanningEmployees(List<Employee> employees) {
        String currentBadge = selectedPlanningBadge();
        planningEmployeeCombo.removeAllItems();
        for (Employee employee : employees) {
            planningEmployeeCombo.addItem(employee.getBadgeNumber() + " - " + employee.fullName().trim());
        }
        if (currentBadge != null && !currentBadge.isBlank()) {
            for (int i = 0; i < planningEmployeeCombo.getItemCount(); i++) {
                String item = planningEmployeeCombo.getItemAt(i);
                if (item.startsWith(currentBadge + " -")) {
                    planningEmployeeCombo.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    public String selectedPlanningBadge() {
        Object selected = planningEmployeeCombo.getSelectedItem();
        if (selected == null) {
            return "";
        }
        String text = selected.toString();
        int separator = text.indexOf(" - ");
        return separator >= 0 ? text.substring(0, separator).trim() : text.trim();
    }

    public static final class ShiftRow {
        public final LocalDate date;
        public final String dayLabel;
        public boolean storeOpen;
        public boolean absence;
        public String absenceType = "";
        public String startText = "";
        public String endText = "";
        public String breakPlan = "";
        public String paidHours = "";
        public String status = "";
        public boolean valid = true;

        public ShiftRow(LocalDate date, String dayLabel, boolean storeOpen) {
            this.date = date;
            this.dayLabel = dayLabel;
            this.storeOpen = storeOpen;
        }

        public boolean hasShiftInput() {
            return !startText.isBlank() || !endText.isBlank();
        }
    }

    public static final class NeedBar {
        public final LocalDate date;
        public final int minuteOfDay;
        public final int needed;
        public final int required;
        public final int planned;

        public NeedBar(LocalDate date, int minuteOfDay, int needed, int required, int planned) {
            this.date = date;
            this.minuteOfDay = minuteOfDay;
            this.needed = needed;
            this.required = required;
            this.planned = planned;
        }
    }

    public interface TimelineActionHandler {
        void addShift(LocalDate date);
        void deleteShift(LocalDate date);
        void shiftChanged(LocalDate date, String startTime, String endTime);
    }

    public interface NeedEditorApplyHandler {
        void apply(List<NeedEditorSlot> slots, int maxValue);
    }

    public static final class NeedEditorSlot {
        public final LocalDate date;
        public final int minuteOfDay;
        public int value;

        public NeedEditorSlot(LocalDate date, int minuteOfDay, int value) {
            this.date = date;
            this.minuteOfDay = minuteOfDay;
            this.value = value;
        }

        public NeedEditorSlot copy() {
            return new NeedEditorSlot(date, minuteOfDay, value);
        }
    }

    public static final class WeeklyScheduleTableModel extends AbstractTableModel {
        private final String[] columns = {"Jour", "Date", "Ouverture", "Debut", "Fin", "Pauses", "H effect.", "Statut"};
        private final List<ShiftRow> rows = new ArrayList<>();

        public void setRows(List<ShiftRow> newRows) {
            rows.clear();
            rows.addAll(newRows);
            fireTableDataChanged();
        }

        public List<ShiftRow> rows() {
            return rows;
        }

        public void refreshRows() {
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 3 || columnIndex == 4;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            ShiftRow row = rows.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> row.dayLabel;
                case 1 -> row.date.toString();
                case 2 -> row.storeOpen ? "Ouvert" : "Ferme";
                case 3 -> row.startText;
                case 4 -> row.endText;
                case 5 -> row.breakPlan;
                case 6 -> row.paidHours;
                case 7 -> row.status;
                default -> "";
            };
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            ShiftRow row = rows.get(rowIndex);
            String text = value == null ? "" : value.toString().trim();
            if (columnIndex == 3) {
                row.startText = text;
            } else if (columnIndex == 4) {
                row.endText = text;
            }
            fireTableRowsUpdated(rowIndex, rowIndex);
        }
    }

    public static final class WeeklyTimelinePanel extends JPanel {
        private List<ShiftRow> rows = new ArrayList<>();
        private TimelineActionHandler actionHandler;
        private ShiftRow draggedRow;
        private DragMode dragMode = DragMode.NONE;
        private int dragOriginMinute;
        private int dragOriginStartMinute;
        private int dragOriginEndMinute;
        // Editeur timeline borne aux horaires d'ouverture et aligne sur des quarts d'heure.
        private static final int SNAP_MINUTES = 15;
        private static final int OPEN_MINUTE = 8 * 60;
        private static final int CLOSE_MINUTE = 20 * 60;
        private static final int HANDLE_WIDTH = 10;

        private enum DragMode {
            NONE,
            LEFT,
            RIGHT,
            BODY
        }

        public void setRows(List<ShiftRow> rows) {
            this.rows = new ArrayList<>(rows);
            repaint();
        }

        public void setActionHandler(TimelineActionHandler actionHandler) {
            this.actionHandler = actionHandler;
        }

        public WeeklyTimelinePanel() {
            TimelineMouseAdapter mouseAdapter = new TimelineMouseAdapter();
            addMouseListener(mouseAdapter);
            addMouseMotionListener(mouseAdapter);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            TimelineLayout layout = timelineLayout();
            int left = layout.left;
            int right = layout.right;
            int top = layout.top;
            int rowHeight = layout.rowHeight;
            int width = layout.width;

            g.setColor(new Color(80, 80, 80));
            for (int hour = 8; hour <= 20; hour += 2) {
                int x = xForMinute(layout, hour * 60);
                g.drawLine(x, top - 12, x, getHeight() - 16);
                g.drawString(String.format("%02dh", hour), x - 11, top - 16);
            }

            for (int i = 0; i < rows.size(); i++) {
                ShiftRow row = rows.get(i);
                int y = top + i * rowHeight;
                g.setColor(row.valid ? new Color(245, 247, 250) : new Color(255, 225, 225));
                g.fillRect(left, y + 3, width, rowHeight - 6);
                g.setColor(new Color(210, 215, 220));
                g.drawLine(left, y + rowHeight - 3, left + width, y + rowHeight - 3);
                g.setColor(new Color(45, 45, 45));
                g.drawString(row.dayLabel.substring(0, Math.min(3, row.dayLabel.length())), 12, y + rowHeight - 9);
                g.drawString(row.date.toString().substring(5), 44, y + rowHeight - 9);

                LocalTime start = parseTime(row.startText);
                LocalTime end = parseTime(row.endText);
                if (row.absence) {
                    drawCentered(g, row.absenceType, left, y + 4, width, rowHeight - 8, new Color(30, 65, 115));
                } else if (!row.storeOpen) {
                    String label = row.status.isBlank() ? "Ferme" : row.status;
                    drawCentered(g, label, left, y + 4, width, rowHeight - 8, new Color(105, 105, 105));
                } else if (start != null && end != null) {
                    int x1 = xForMinute(layout, minutes(start));
                    int x2 = xForMinute(layout, minutes(end));
                    x1 = Math.max(left, Math.min(left + width, x1));
                    x2 = Math.max(left, Math.min(left + width, x2));
                    int barWidth = Math.max(4, x2 - x1);
                    g.setColor(row.valid ? new Color(42, 135, 139) : new Color(205, 65, 65));
                    g.fillRoundRect(x1, y + 7, barWidth, rowHeight - 14, 5, 5);
                    g.setColor(row.valid ? new Color(22, 92, 96) : new Color(135, 35, 35));
                    g.drawRoundRect(x1, y + 7, barWidth, rowHeight - 14, 5, 5);
                    g.setColor(new Color(245, 250, 250, 160));
                    g.fillRect(x1, y + 8, Math.min(HANDLE_WIDTH, barWidth), rowHeight - 16);
                    g.fillRect(x2 - Math.min(HANDLE_WIDTH, barWidth), y + 8, Math.min(HANDLE_WIDTH, barWidth), rowHeight - 16);
                    g.setColor(Color.WHITE);
                    g.drawString(row.startText + "-" + row.endText, x1 + 6, y + rowHeight - 10);
                    if (!row.status.isBlank() && !row.status.equals("OK")) {
                        g.setColor(row.valid ? new Color(80, 80, 80) : new Color(135, 35, 35));
                        g.drawString(row.status, Math.min(x2 + 8, left + width - 190), y + rowHeight - 10);
                    }
                } else if (row.storeOpen) {
                    drawCentered(g, "Clic droit pour ajouter un horaire", left, y + 4, width, rowHeight - 8, new Color(105, 105, 105));
                }
            }
            g.dispose();
        }

        private TimelineLayout timelineLayout() {
            int left = 98;
            int right = 18;
            int top = 34;
            int rowHeight = Math.max(28, (getHeight() - top - 20) / Math.max(1, rows.size()));
            int width = Math.max(1, getWidth() - left - right);
            return new TimelineLayout(left, right, top, rowHeight, width);
        }

        private int rowIndexAt(int y) {
            TimelineLayout layout = timelineLayout();
            int index = (y - layout.top) / layout.rowHeight;
            if (index < 0 || index >= rows.size()) {
                return -1;
            }
            return index;
        }

        private int xForMinute(TimelineLayout layout, int minute) {
            return layout.left + (int) Math.round(layout.width * ((minute - OPEN_MINUTE) / (double) (CLOSE_MINUTE - OPEN_MINUTE)));
        }

        private int minuteForX(TimelineLayout layout, int x) {
            int clampedX = Math.max(layout.left, Math.min(layout.left + layout.width, x));
            double ratio = (clampedX - layout.left) / (double) layout.width;
            int minute = OPEN_MINUTE + (int) Math.round((CLOSE_MINUTE - OPEN_MINUTE) * ratio);
            return snapMinute(minute);
        }

        private int snapMinute(int minute) {
            int snapped = Math.round(minute / (float) SNAP_MINUTES) * SNAP_MINUTES;
            return Math.max(OPEN_MINUTE, Math.min(CLOSE_MINUTE, snapped));
        }

        private String formatMinute(int minute) {
            int hour = minute / 60;
            int minutes = minute % 60;
            return String.format("%02d:%02d", hour, minutes);
        }

        private DragMode dragModeAt(ShiftRow row, int x) {
            LocalTime start = parseTime(row.startText);
            LocalTime end = parseTime(row.endText);
            if (start == null || end == null || !row.storeOpen || row.absence) {
                return DragMode.NONE;
            }
            TimelineLayout layout = timelineLayout();
            int x1 = xForMinute(layout, minutes(start));
            int x2 = xForMinute(layout, minutes(end));
            // Bords: redimensionnement, milieu: deplacement de toute la plage.
            if (Math.abs(x - x1) <= HANDLE_WIDTH + 3) {
                return DragMode.LEFT;
            }
            if (Math.abs(x - x2) <= HANDLE_WIDTH + 3) {
                return DragMode.RIGHT;
            }
            if (x > x1 + HANDLE_WIDTH && x < x2 - HANDLE_WIDTH) {
                return DragMode.BODY;
            }
            return DragMode.NONE;
        }

        private void showMenu(ShiftRow row, int x, int y) {
            JPopupMenu menu = new JPopupMenu();
            JMenuItem addItem = new JMenuItem("Ajouter horaire");
            JMenuItem deleteItem = new JMenuItem("Supprimer horaires");
            addItem.setEnabled(row.storeOpen && !row.absence);
            deleteItem.setEnabled(row.hasShiftInput());
            addItem.addActionListener(e -> {
                if (actionHandler != null) {
                    actionHandler.addShift(row.date);
                }
            });
            deleteItem.addActionListener(e -> {
                if (actionHandler != null) {
                    actionHandler.deleteShift(row.date);
                }
            });
            menu.add(addItem);
            menu.add(deleteItem);
            menu.show(this, x, y);
        }

        private final class TimelineMouseAdapter extends MouseAdapter {
            @Override
            public void mousePressed(MouseEvent event) {
                int rowIndex = rowIndexAt(event.getY());
                if (rowIndex < 0) {
                    return;
                }
                ShiftRow row = rows.get(rowIndex);
                // Clic droit sur la ligne: menu contextuel ajouter/supprimer.
                if (event.isPopupTrigger() || MouseEvent.BUTTON3 == event.getButton()) {
                    showMenu(row, event.getX(), event.getY());
                    return;
                }
                dragMode = dragModeAt(row, event.getX());
                draggedRow = dragMode == DragMode.NONE ? null : row;
                if (draggedRow != null) {
                    LocalTime start = parseTime(draggedRow.startText);
                    LocalTime end = parseTime(draggedRow.endText);
                    if (start == null || end == null) {
                        draggedRow = null;
                        dragMode = DragMode.NONE;
                        return;
                    }
                    TimelineLayout layout = timelineLayout();
                    dragOriginMinute = minuteForX(layout, event.getX());
                    dragOriginStartMinute = minutes(start);
                    dragOriginEndMinute = minutes(end);
                }
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                if (event.isPopupTrigger()) {
                    int rowIndex = rowIndexAt(event.getY());
                    if (rowIndex >= 0) {
                        showMenu(rows.get(rowIndex), event.getX(), event.getY());
                    }
                }
                draggedRow = null;
                dragMode = DragMode.NONE;
                setCursor(Cursor.getDefaultCursor());
            }

            @Override
            public void mouseDragged(MouseEvent event) {
                if (draggedRow == null || dragMode == DragMode.NONE) {
                    return;
                }
                LocalTime start = parseTime(draggedRow.startText);
                LocalTime end = parseTime(draggedRow.endText);
                if (start == null || end == null) {
                    return;
                }
                TimelineLayout layout = timelineLayout();
                int selectedMinute = minuteForX(layout, event.getX());
                int startMinute = minutes(start);
                int endMinute = minutes(end);
                if (dragMode == DragMode.LEFT) {
                    startMinute = Math.min(selectedMinute, endMinute - SNAP_MINUTES);
                } else if (dragMode == DragMode.RIGHT) {
                    endMinute = Math.max(selectedMinute, startMinute + SNAP_MINUTES);
                } else if (dragMode == DragMode.BODY) {
                    // Glissement du corps: conserve la duree et translate le bloc complet.
                    int duration = dragOriginEndMinute - dragOriginStartMinute;
                    int delta = selectedMinute - dragOriginMinute;
                    startMinute = dragOriginStartMinute + delta;
                    endMinute = dragOriginEndMinute + delta;
                    // Bornage strict dans la fenetre 08:00-20:00.
                    if (startMinute < OPEN_MINUTE) {
                        startMinute = OPEN_MINUTE;
                        endMinute = startMinute + duration;
                    }
                    if (endMinute > CLOSE_MINUTE) {
                        endMinute = CLOSE_MINUTE;
                        startMinute = endMinute - duration;
                    }
                }
                draggedRow.startText = formatMinute(startMinute);
                draggedRow.endText = formatMinute(endMinute);
                if (actionHandler != null) {
                    actionHandler.shiftChanged(draggedRow.date, draggedRow.startText, draggedRow.endText);
                }
                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent event) {
                int rowIndex = rowIndexAt(event.getY());
                if (rowIndex < 0) {
                    setCursor(Cursor.getDefaultCursor());
                    return;
                }
                DragMode mode = dragModeAt(rows.get(rowIndex), event.getX());
                if (mode == DragMode.BODY) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                } else {
                    setCursor(mode == DragMode.NONE ? Cursor.getDefaultCursor() : Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
                }
            }
        }

        private record TimelineLayout(int left, int right, int top, int rowHeight, int width) {
        }

        private static LocalTime parseTime(String text) {
            try {
                return text == null || text.isBlank() ? null : LocalTime.parse(text.trim());
            } catch (DateTimeParseException ex) {
                return null;
            }
        }

        private static int minutes(LocalTime time) {
            return time.getHour() * 60 + time.getMinute();
        }

        private static void drawCentered(Graphics2D g, String text, int x, int y, int width, int height, Color color) {
            FontMetrics metrics = g.getFontMetrics();
            int textWidth = metrics.stringWidth(text);
            g.setColor(color);
            g.drawString(text, x + Math.max(4, (width - textWidth) / 2), y + (height + metrics.getAscent()) / 2 - 2);
        }
    }

    public static final class NeedDayEditorPanel extends JPanel {
        private static final int OPEN_MINUTE = 8 * 60;
        private static final int CLOSE_MINUTE = 20 * 60;
        private static final int SNAP_MINUTES = 15;
        private final List<NeedEditorSlot> slots;
        private int maxValue;

        public NeedDayEditorPanel(List<NeedEditorSlot> slots, int maxValue) {
            this.slots = new ArrayList<>(slots);
            this.maxValue = Math.max(1, maxValue);
            setPreferredSize(new Dimension(980, 420));
            NeedBarMouseAdapter mouseAdapter = new NeedBarMouseAdapter();
            addMouseListener(mouseAdapter);
            addMouseMotionListener(mouseAdapter);
        }

        public void setMaxValue(int maxValue) {
            this.maxValue = Math.max(1, maxValue);
            clampValues();
        }

        public void setAllValues(int value) {
            int safeValue = Math.max(0, Math.min(maxValue, value));
            for (NeedEditorSlot slot : slots) {
                slot.value = safeValue;
            }
            repaint();
        }

        public void clampValues() {
            for (NeedEditorSlot slot : slots) {
                slot.value = Math.max(0, Math.min(maxValue, slot.value));
            }
            repaint();
        }

        public List<NeedEditorSlot> slots() {
            List<NeedEditorSlot> result = new ArrayList<>();
            for (NeedEditorSlot slot : slots) {
                result.add(slot.copy());
            }
            return result;
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            NeedChartLayout layout = chartLayout();
            int baseY = layout.top + layout.height;
            g.setColor(new Color(235, 238, 242));
            for (int value = 0; value <= maxValue; value++) {
                int y = baseY - (int) Math.round(layout.height * (value / (double) maxValue));
                g.drawLine(layout.left, y, layout.left + layout.width, y);
                if (value == 0 || value == maxValue || value % 2 == 0) {
                    g.setColor(new Color(70, 70, 70));
                    g.drawString(Integer.toString(value), 12, y + 4);
                    g.setColor(new Color(235, 238, 242));
                }
            }

            g.setColor(new Color(70, 70, 70));
            g.setStroke(new BasicStroke(1.2f));
            g.drawLine(layout.left, layout.top, layout.left, baseY);
            g.drawLine(layout.left, baseY, layout.left + layout.width, baseY);

            if (slots.isEmpty()) {
                g.drawString("Aucun creneau", layout.left + 12, layout.top + 24);
                g.dispose();
                return;
            }

            int groupWidth = Math.max(1, layout.width / slots.size());
            int barWidth = Math.max(2, groupWidth - 1);
            for (int i = 0; i < slots.size(); i++) {
                NeedEditorSlot slot = slots.get(i);
                int x = layout.left + i * groupWidth;
                int value = Math.max(0, Math.min(maxValue, slot.value));
                int height = value == 0 ? 4 : Math.max(4, (int) Math.round(layout.height * (value / (double) maxValue)));
                // Vert = besoin positif ; jaune = pas de besoin sur le quart d'heure.
                g.setColor(value > 0 ? new Color(53, 142, 88) : new Color(222, 192, 59));
                g.fillRect(x, baseY - height, barWidth, height);

                if (slot.minuteOfDay % 120 == 0) {
                    g.setColor(new Color(70, 70, 70));
                    g.drawLine(x, baseY, x, baseY + 5);
                    g.drawString(String.format("%02dh", slot.minuteOfDay / 60), x - 6, baseY + 20);
                }
            }

            g.setColor(new Color(55, 55, 55));
            g.drawString("Besoin par quart d'heure", layout.left, 18);
            g.dispose();
        }

        private NeedChartLayout chartLayout() {
            int left = 42;
            int right = 18;
            int top = 28;
            int bottom = 36;
            int width = Math.max(1, getWidth() - left - right);
            int height = Math.max(1, getHeight() - top - bottom);
            return new NeedChartLayout(left, top, width, height);
        }

        private int slotIndexAt(int x) {
            NeedChartLayout layout = chartLayout();
            if (x < layout.left || x > layout.left + layout.width || slots.isEmpty()) {
                return -1;
            }
            int groupWidth = Math.max(1, layout.width / slots.size());
            int index = (x - layout.left) / groupWidth;
            if (index < 0 || index >= slots.size()) {
                return -1;
            }
            return index;
        }

        private int valueAt(int y) {
            NeedChartLayout layout = chartLayout();
            int baseY = layout.top + layout.height;
            int clampedY = Math.max(layout.top, Math.min(baseY, y));
            // Plus on monte, plus le besoin augmente (mapping vertical -> valeur).
            double ratio = (baseY - clampedY) / (double) layout.height;
            return Math.max(0, Math.min(maxValue, (int) Math.round(ratio * maxValue)));
        }

        private final class NeedBarMouseAdapter extends MouseAdapter {
            @Override
            public void mousePressed(MouseEvent event) {
                updateSlot(event);
            }

            @Override
            public void mouseDragged(MouseEvent event) {
                updateSlot(event);
            }

            @Override
            public void mouseMoved(MouseEvent event) {
                setCursor(slotIndexAt(event.getX()) >= 0
                    ? Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR)
                    : Cursor.getDefaultCursor());
            }

            private void updateSlot(MouseEvent event) {
                int index = slotIndexAt(event.getX());
                if (index < 0) {
                    return;
                }
                // Drag continu: on ecrit la valeur directement dans la barre survolee.
                slots.get(index).value = valueAt(event.getY());
                repaint();
            }
        }

        private record NeedChartLayout(int left, int top, int width, int height) {
        }
    }

    public static final class StaffingNeedsChart extends JPanel {
        private List<NeedBar> bars = new ArrayList<>();

        public void setBars(List<NeedBar> bars) {
            this.bars = new ArrayList<>(bars);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int left = 42;
            int right = 18;
            int top = 22;
            int bottom = 42;
            int chartWidth = Math.max(1, getWidth() - left - right);
            int chartHeight = Math.max(1, getHeight() - top - bottom);
            int maxPositive = Math.max(1, bars.stream().mapToInt(bar -> Math.max(0, bar.needed)).max().orElse(0));
            int maxNegative = Math.max(1, bars.stream().mapToInt(bar -> Math.max(0, -bar.needed)).max().orElse(0));
            int totalScale = maxPositive + maxNegative;
            int zeroY = top + (int) Math.round(chartHeight * (maxPositive / (double) totalScale));

            g.setColor(new Color(235, 238, 242));
            for (int i = 0; i <= maxPositive; i++) {
                int y = zeroY - (int) Math.round((zeroY - top) * (i / (double) maxPositive));
                g.drawLine(left, y, left + chartWidth, y);
            }
            for (int i = 1; i <= maxNegative; i++) {
                int y = zeroY + (int) Math.round((top + chartHeight - zeroY) * (i / (double) maxNegative));
                g.drawLine(left, y, left + chartWidth, y);
            }

            g.setColor(new Color(70, 70, 70));
            g.setStroke(new BasicStroke(1.2f));
            g.drawLine(left, top, left, top + chartHeight);
            g.drawLine(left, zeroY, left + chartWidth, zeroY);

            if (bars.isEmpty()) {
                g.drawString("Aucune donnee planning", left + 12, top + 24);
                g.dispose();
                return;
            }

            int groupWidth = Math.max(1, chartWidth / bars.size());
            int barWidth = Math.max(1, groupWidth - 1);
            LocalDate previousDate = null;
            for (int i = 0; i < bars.size(); i++) {
                NeedBar bar = bars.get(i);
                int x = left + i * groupWidth;
                int baseY = zeroY;
                int positiveHeight = Math.max(4, (int) Math.round((zeroY - top) * (bar.needed / (double) maxPositive)));
                int negativeHeight = Math.max(4, (int) Math.round((top + chartHeight - zeroY) * (-bar.needed / (double) maxNegative)));

                if (previousDate == null || !previousDate.equals(bar.date)) {
                    g.setColor(new Color(185, 190, 198));
                    g.drawLine(x, top, x, top + chartHeight);
                    g.setColor(new Color(70, 70, 70));
                    g.drawString(dayShort(bar.date), x + 2, top + chartHeight + 16);
                    previousDate = bar.date;
                }

                if (bar.needed > 0) {
                    g.setColor(new Color(53, 142, 88));
                    g.fillRect(x, baseY - positiveHeight, barWidth, positiveHeight);
                } else if (bar.needed < 0) {
                    g.setColor(new Color(222, 192, 59));
                    g.fillRect(x, baseY, barWidth, negativeHeight);
                } else {
                    g.setColor(new Color(222, 192, 59));
                    g.fillRect(x, baseY - 2, barWidth, 4);
                }

                if (bar.minuteOfDay % 120 == 0) {
                    g.setColor(new Color(70, 70, 70));
                    g.drawLine(x, zeroY, x, zeroY + 4);
                    if (bar.minuteOfDay == 8 * 60 || bar.minuteOfDay == 12 * 60 || bar.minuteOfDay == 16 * 60) {
                        g.drawString(String.format("%02dh", bar.minuteOfDay / 60), x - 5, top + chartHeight + 31);
                    }
                }
            }

            g.setColor(new Color(53, 142, 88));
            g.fillRect(left + 8, 6, 10, 10);
            g.setColor(new Color(55, 55, 55));
            g.drawString("Besoin restant > 0", left + 22, 15);
            g.setColor(new Color(222, 192, 59));
            g.fillRect(left + 148, 6, 10, 10);
            g.setColor(new Color(55, 55, 55));
            g.drawString("Surplus / 0", left + 162, 15);
            g.dispose();
        }

        private String dayShort(LocalDate date) {
            return StaffView.dayShort(date);
        }
    }

    private static String dayShort(LocalDate date) {
        if (date == null) {
            return "";
        }
        return switch (date.getDayOfWeek()) {
            case MONDAY -> "Lun";
            case TUESDAY -> "Mar";
            case WEDNESDAY -> "Mer";
            case THURSDAY -> "Jeu";
            case FRIDAY -> "Ven";
            case SATURDAY -> "Sam";
            case SUNDAY -> "Dim";
        };
    }
}
