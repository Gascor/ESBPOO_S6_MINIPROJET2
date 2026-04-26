package com.leadelmarche.ui.mvc.controller;

import com.leadelmarche.common.Address;
import com.leadelmarche.domain.people.ContractType;
import com.leadelmarche.domain.people.Employee;
import com.leadelmarche.domain.people.StaffAbsence;
import com.leadelmarche.domain.people.WorkShift;
import com.leadelmarche.service.AbsenceService;
import com.leadelmarche.service.ScheduleService;
import com.leadelmarche.service.ScheduleService.ShiftValidation;
import com.leadelmarche.service.StaffService;
import com.leadelmarche.ui.mvc.view.StaffView;
import com.leadelmarche.ui.mvc.view.StaffView.NeedBar;
import com.leadelmarche.ui.mvc.view.StaffView.NeedEditorSlot;
import com.leadelmarche.ui.mvc.view.StaffView.ShiftRow;
import java.awt.Color;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;

public class StaffController {
    private final StaffService staffService;
    private final AbsenceService absenceService;
    private final ScheduleService scheduleService;
    private final StaffView view;
    private final Map<String, Integer> manualNeeds = new HashMap<>();
    private int manualNeedsMax = 8;
    private boolean planningRefreshInProgress;

    public StaffController(StaffService staffService, AbsenceService absenceService, ScheduleService scheduleService) {
        this.staffService = staffService;
        this.absenceService = absenceService;
        this.scheduleService = scheduleService;
        this.view = new StaffView();
        bindActions();
        initializePlanningSelector();
        refreshEmployees();
        loadPlanningWeek();
    }

    public void show() {
        view.setVisible(true);
    }

    private void bindActions() {
        view.addButton().addActionListener(e -> addEmployee());
        view.updateButton().addActionListener(e -> updateEmployee());
        view.deactivateButton().addActionListener(e -> deactivateEmployee());
        view.searchButton().addActionListener(e -> searchEmployees());
        view.refreshButton().addActionListener(e -> refreshEmployees());
        view.addAbsenceButton().addActionListener(e -> addAbsence());
        view.generateScheduleButton().addActionListener(e -> generateSchedule());
        view.exportAllPdfButton().addActionListener(e -> exportSchedulePdfAll());
        view.exportSelectedPdfButton().addActionListener(e -> exportSchedulePdfByBadge());
        view.forecastButton().addActionListener(e -> runForecast());
        view.manageNeedsButton().addActionListener(e -> openNeedEditor());
        view.loadPlanningButton().addActionListener(e -> loadPlanningWeek());
        view.saveManualPlanningButton().addActionListener(e -> saveManualPlanning());
        view.planningYearField().addActionListener(e -> updatePlanningWeeksFromYear());
        view.planningWeekCombo().addActionListener(e -> loadPlanningWeek());
        view.planningEmployeeCombo().addActionListener(e -> loadPlanningWeek());
        view.planningOvertimeHoursField().addActionListener(e -> validatePlanningRows());
        view.planningOvertimeHoursField().getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                validatePlanningRows();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                validatePlanningRows();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                validatePlanningRows();
            }
        });
        view.planningTableModel().addTableModelListener(e -> {
            if (!planningRefreshInProgress && e.getType() == TableModelEvent.UPDATE) {
                validatePlanningRows();
            }
        });
        view.setTimelineActionHandler(new StaffView.TimelineActionHandler() {
            @Override
            public void addShift(LocalDate date) {
                addShiftOnTimeline(date);
            }

            @Override
            public void deleteShift(LocalDate date) {
                deleteShiftOnTimeline(date);
            }

            @Override
            public void shiftChanged(LocalDate date, String startTime, String endTime) {
                validatePlanningRows();
            }
        });
    }

    private void initializePlanningSelector() {
        LocalDate today = LocalDate.now();
        int year = scheduleService.isoWeekYear(today);
        int week = scheduleService.isoWeekNumber(today);
        planningRefreshInProgress = true;
        view.planningYearField().setText(Integer.toString(year));
        view.setPlanningWeeks(scheduleService.weeksInYear(year), week);
        view.weekStartField().setText(scheduleService.mondayOfIsoWeek(year, week).toString());
        planningRefreshInProgress = false;
    }

    private void updatePlanningWeeksFromYear() {
        if (planningRefreshInProgress) {
            return;
        }
        int year = parseInt(view.planningYearField().getText().trim(), LocalDate.now().getYear());
        Integer selected = (Integer) view.planningWeekCombo().getSelectedItem();
        int week = selected == null ? 1 : selected;
        planningRefreshInProgress = true;
        view.setPlanningWeeks(scheduleService.weeksInYear(year), week);
        planningRefreshInProgress = false;
        loadPlanningWeek();
    }

    private void addEmployee() {
        try {
            Employee employee = new Employee();
            employee.setBadgeNumber(view.badgeField().getText().trim());
            employee.setFirstName(view.firstNameField().getText().trim());
            employee.setLastName(view.lastNameField().getText().trim());
            employee.setRole(view.roleField().getText().trim());
            employee.setSupervisorBadge(view.supervisorField().getText().trim());
            employee.setHomeAddress(new Address("", "", "", ""));
            employee.setWorkAddress(new Address("LeadelMarche", "Versailles", "78000", "France"));
            ContractType selectedContract = view.contractTypeCombo().getItemAt(view.contractTypeCombo().getSelectedIndex());
            employee.setContractType(selectedContract);
            employee.setContractWeeklyHours(parseInt(view.contractHoursField().getText().trim(), 35));
            staffService.createEmployee(employee);
            refreshEmployees();
            loadPlanningWeek();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void searchEmployees() {
        render(staffService.searchEmployees(view.searchField().getText()));
    }

    private void refreshEmployees() {
        List<Employee> employees = staffService.listEmployees(true);
        view.setPlanningEmployees(employees);
        render(employees);
    }

    private void updateEmployee() {
        try {
            String employeeId = view.editEmployeeIdField().getText().trim();
            if (employeeId.isBlank()) {
                throw new IllegalArgumentException("ID employe obligatoire pour la mise a jour");
            }
            Employee employee = staffService.findById(employeeId).orElseThrow(
                () -> new IllegalStateException("Employe introuvable: " + employeeId)
            );
            if (!view.badgeField().getText().trim().isBlank()) {
                employee.setBadgeNumber(view.badgeField().getText().trim());
            }
            if (!view.firstNameField().getText().trim().isBlank()) {
                employee.setFirstName(view.firstNameField().getText().trim());
            }
            if (!view.lastNameField().getText().trim().isBlank()) {
                employee.setLastName(view.lastNameField().getText().trim());
            }
            if (!view.roleField().getText().trim().isBlank()) {
                employee.setRole(view.roleField().getText().trim());
            }
            if (!view.supervisorField().getText().trim().isBlank()) {
                employee.setSupervisorBadge(view.supervisorField().getText().trim());
            }
            ContractType selectedContract = view.contractTypeCombo().getItemAt(view.contractTypeCombo().getSelectedIndex());
            employee.setContractType(selectedContract);
            if (!view.contractHoursField().getText().trim().isBlank()) {
                employee.setContractWeeklyHours(parseInt(view.contractHoursField().getText().trim(), employee.getContractWeeklyHours()));
            }
            staffService.updateEmployee(employee);
            refreshEmployees();
            loadPlanningWeek();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void deactivateEmployee() {
        try {
            String employeeId = view.editEmployeeIdField().getText().trim();
            if (employeeId.isBlank()) {
                throw new IllegalArgumentException("ID employe obligatoire pour la desactivation");
            }
            staffService.deactivateEmployee(employeeId);
            refreshEmployees();
            loadPlanningWeek();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void render(List<Employee> employees) {
        StringBuilder sb = new StringBuilder();
        sb.append("ID | BADGE | NOM | PRENOM | ROLE | CONTRAT | H/SEM | SUP\n");
        sb.append("---------------------------------------------------------------------------------\n");
        for (Employee e : employees) {
            sb.append(e.getId()).append(" | ")
                .append(e.getBadgeNumber()).append(" | ")
                .append(e.getLastName()).append(" | ")
                .append(e.getFirstName()).append(" | ")
                .append(e.getRole()).append(" | ")
                .append(e.getContractType()).append(" | ")
                .append(e.getContractWeeklyHours()).append(" | ")
                .append(e.getSupervisorBadge()).append('\n');
        }
        view.outputArea().setText(sb.toString());
    }

    private void addAbsence() {
        try {
            String badge = view.absenceBadgeField().getText().trim();
            LocalDate date = LocalDate.parse(view.absenceDateField().getText().trim());
            String type = view.absenceTypeCombo().getSelectedItem().toString();
            String note = view.absenceNoteField().getText().trim();
            absenceService.recordAbsence(badge, date, type, note);
            view.outputArea().append("\nAbsence saisie: " + badge + " - " + date + " (" + type + ")\n");
            loadPlanningWeek();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void generateSchedule() {
        try {
            LocalDate weekStart = LocalDate.parse(view.weekStartField().getText().trim());
            int minStaff = parseInt(view.minStaffField().getText().trim(), 3);
            List<WorkShift> shifts = scheduleService.generateWeeklySchedule(weekStart, minStaff);
            syncPlanningSelector(weekStart);
            loadPlanningWeek();
            StringBuilder sb = new StringBuilder(view.outputArea().getText());
            sb.append("\n\nPLANNING GENERE (semaine ").append(scheduleService.mondayOfIsoWeek(
                scheduleService.isoWeekYear(weekStart), scheduleService.isoWeekNumber(weekStart)
            )).append(")\n");
            sb.append("Badge | Nom | Date | Debut | Fin | Pauses | Taux\n");
            sb.append("--------------------------------------------------------------------------\n");
            for (WorkShift shift : shifts) {
                sb.append(shift.getBadgeNumber()).append(" | ")
                    .append(shift.getEmployeeName()).append(" | ")
                    .append(shift.getShiftDate()).append(" | ")
                    .append(shift.getStartTime()).append(" | ")
                    .append(shift.getEndTime()).append(" | ")
                    .append(shift.getBreakPlan()).append(" | ")
                    .append(shift.getPaidMultiplier()).append('\n');
            }
            view.outputArea().setText(sb.toString());
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void loadPlanningWeek() {
        if (planningRefreshInProgress) {
            return;
        }
        try {
            String badge = view.selectedPlanningBadge();
            if (badge.isBlank()) {
                planningRefreshInProgress = true;
                view.planningTableModel().setRows(List.of());
                view.planningContractHoursField().setText("");
                view.planningOvertimeHoursField().setText("0");
                planningRefreshInProgress = false;
                view.timelinePanel().setRows(List.of());
                view.needsChart().setBars(List.of());
                return;
            }
            LocalDate monday = selectedPlanningMonday();
            view.weekStartField().setText(monday.toString());
            updatePlanningContractHours(badge);

            Map<LocalDate, WorkShift> shiftByDate = scheduleService.getWeekSchedule(monday).stream()
                .filter(shift -> badge.equalsIgnoreCase(shift.getBadgeNumber()))
                .collect(Collectors.toMap(WorkShift::getShiftDate, shift -> shift, (first, second) -> second));

            Map<LocalDate, String> absenceByDate = absenceService.listAbsences(true).stream()
                .filter(absence -> badge.equalsIgnoreCase(absence.getBadgeNumber()))
                .filter(absence -> !absence.getAbsenceDate().isBefore(monday) && !absence.getAbsenceDate().isAfter(monday.plusDays(6)))
                .collect(Collectors.toMap(StaffAbsence::getAbsenceDate, StaffAbsence::getType, (first, second) -> first));

            List<ShiftRow> rows = new ArrayList<>();
            for (int day = 0; day <= 6; day++) {
                LocalDate date = monday.plusDays(day);
                ShiftRow row = new ShiftRow(date, dayLabel(date.getDayOfWeek()), scheduleService.isStoreOpen(date));
                WorkShift shift = shiftByDate.get(date);
                if (shift != null) {
                    row.startText = shift.getStartTime().toString();
                    row.endText = shift.getEndTime().toString();
                }
                String absenceType = absenceByDate.get(date);
                if (absenceType != null) {
                    row.absence = true;
                    row.absenceType = absenceType;
                } else if (date.getDayOfWeek() == DayOfWeek.SUNDAY && !scheduleService.isStoreOpen(date)) {
                    row.absence = true;
                    row.absenceType = "CONGE HEBDOMADAIRE";
                }
                rows.add(row);
            }

            planningRefreshInProgress = true;
            view.planningTableModel().setRows(rows);
            planningRefreshInProgress = false;
            validatePlanningRows();
            updateNeedsChart(monday);
        } catch (Exception ex) {
            planningRefreshInProgress = false;
            showError(ex);
        }
    }

    private void validatePlanningRows() {
        // Evite les boucles de validation quand on rafraichit le modele depuis le code.
        if (planningRefreshInProgress) {
            return;
        }
        String badge = view.selectedPlanningBadge();
        Employee employee = staffService.findByBadge(badge).orElse(null);
        if (employee == null) {
            view.planningContractHoursField().setText("");
            return;
        }
        int contractHours = scheduleService.contractWeeklyHours(employee);
        view.planningContractHoursField().setText(Integer.toString(contractHours));
        double paidHolidayCreditHours = scheduleService.paidClosedHolidayCreditHours(contractHours);

        double totalHours = 0;
        double totalPaidHolidayCredit = 0;
        boolean hasInvalid = false;
        for (ShiftRow row : view.planningTableModel().rows()) {
            // Champs calcules a chaque passe (pause, heures payees, statut couleur).
            row.breakPlan = "";
            row.paidHours = "";
            row.valid = true;
            if (row.absence) {
                if (row.hasShiftInput()) {
                    row.valid = false;
                    row.status = row.absenceType + ": supprimer les horaires";
                    hasInvalid = true;
                } else {
                    // Un ferie ferme reste credite en heures meme sans plage horaire.
                    if (scheduleService.isPaidClosedHoliday(row.date)) {
                        row.status = row.absenceType + " + ferie ferme";
                        row.paidHours = formatHours(paidHolidayCreditHours) + "h";
                        totalHours += paidHolidayCreditHours;
                        totalPaidHolidayCredit += paidHolidayCreditHours;
                    } else {
                        row.status = row.absenceType;
                    }
                }
                continue;
            }
            if (!row.storeOpen) {
                if (row.hasShiftInput()) {
                    row.valid = false;
                    row.status = scheduleService.isPaidClosedHoliday(row.date) ? "Jour ferie ferme" : "Magasin ferme";
                    hasInvalid = true;
                } else {
                    // Le dimanche ferme est traite comme repos hebdomadaire (pas "absence non payee").
                    if (scheduleService.isPaidClosedHoliday(row.date)) {
                        row.status = "Ferie ferme (compte en heures)";
                        row.paidHours = formatHours(paidHolidayCreditHours) + "h";
                        totalHours += paidHolidayCreditHours;
                        totalPaidHolidayCredit += paidHolidayCreditHours;
                    } else {
                        row.status = row.date.getDayOfWeek() == DayOfWeek.SUNDAY
                            ? "Repos hebdomadaire"
                            : "Ferme";
                    }
                }
                continue;
            }
            if (!row.hasShiftInput()) {
                row.status = "Repos";
                continue;
            }
            if (row.startText.isBlank() || row.endText.isBlank()) {
                row.valid = false;
                row.status = "Debut et fin obligatoires";
                hasInvalid = true;
                continue;
            }
            try {
                LocalTime start = parseTime(row.startText);
                LocalTime end = parseTime(row.endText);
                ShiftValidation validation = scheduleService.validateShift(employee, row.date, start, end);
                row.valid = validation.isValid();
                row.status = validation.getMessage();
                row.breakPlan = validation.getBreakPlan();
                row.paidHours = validation.getPaidHours() > 0 ? formatHours(validation.getPaidHours()) + "h" : "";
                if (validation.isValid()) {
                    totalHours += validation.getPaidHours();
                } else {
                    hasInvalid = true;
                }
            } catch (DateTimeParseException ex) {
                row.valid = false;
                row.status = "Format heure HH:mm";
                hasInvalid = true;
            }
        }

        double overtimeHours = Math.max(0, parseDouble(view.planningOvertimeHoursField().getText().trim(), 0));
        double allowedHours = contractHours + overtimeHours;
        // Controle global de la semaine (contrat + heures supp autorisees).
        if (totalHours > allowedHours + 0.01) {
            hasInvalid = true;
            for (ShiftRow row : view.planningTableModel().rows()) {
                if (row.hasShiftInput() && row.valid) {
                    row.valid = false;
                    row.status = "Plafond depasse (" + formatHours(totalHours) + "/" + formatHours(allowedHours) + "h)";
                }
            }
        }

        planningRefreshInProgress = true;
        view.planningTableModel().refreshRows();
        planningRefreshInProgress = false;
        // Synchronise la vue timeline et le graphe de besoin apres toute modification.
        view.timelinePanel().setRows(view.planningTableModel().rows());
        view.planningStatusLabel().setForeground(hasInvalid ? new Color(155, 45, 45) : new Color(45, 90, 55));
        view.planningStatusLabel().setText(
            "Total semaine: " + formatHours(totalHours) + "h / autorise " + formatHours(allowedHours)
                + "h (contrat " + contractHours + "h + supp " + formatHours(overtimeHours) + "h)"
                + (totalPaidHolidayCredit > 0 ? " dont " + formatHours(totalPaidHolidayCredit) + "h ferie(s) ferme(s)" : "")
                + " - " + (hasInvalid ? "corriger les lignes rouges avant sauvegarde" : "planning conforme")
        );
        updateNeedsChart(selectedPlanningMonday());
    }

    private void updatePlanningContractHours(String badge) {
        Employee employee = staffService.findByBadge(badge).orElse(null);
        if (employee == null) {
            view.planningContractHoursField().setText("");
            return;
        }
        view.planningContractHoursField().setText(Integer.toString(scheduleService.contractWeeklyHours(employee)));
    }

    private void addShiftOnTimeline(LocalDate date) {
        ShiftRow row = findPlanningRow(date);
        if (row == null) {
            return;
        }
        if (!row.storeOpen || row.absence) {
            showError(new IllegalArgumentException("Impossible d'ajouter un horaire sur un jour ferme ou absent."));
            return;
        }
        if (!row.hasShiftInput()) {
            row.startText = "09:00";
            row.endText = "13:15";
        }
        validatePlanningRows();
    }

    private void deleteShiftOnTimeline(LocalDate date) {
        ShiftRow row = findPlanningRow(date);
        if (row == null) {
            return;
        }
        row.startText = "";
        row.endText = "";
        validatePlanningRows();
    }

    private ShiftRow findPlanningRow(LocalDate date) {
        return view.planningTableModel().rows().stream()
            .filter(row -> date.equals(row.date))
            .findFirst()
            .orElse(null);
    }

    private void saveManualPlanning() {
        try {
            validatePlanningRows();
            List<ShiftRow> rows = view.planningTableModel().rows();
            boolean invalid = rows.stream().anyMatch(row -> !row.valid);
            if (invalid) {
                throw new IllegalArgumentException("Corriger les lignes rouges avant d'enregistrer le planning.");
            }
            String badge = view.selectedPlanningBadge();
            List<StaffAbsence> existingAbsences = absenceService.listAbsences(true).stream()
                .filter(absence -> badge.equalsIgnoreCase(absence.getBadgeNumber()))
                .toList();
            for (ShiftRow row : rows) {
                if (row.hasShiftInput()) {
                    scheduleService.saveManualShift(badge, row.date, parseTime(row.startText), parseTime(row.endText));
                } else {
                    scheduleService.clearShift(badge, row.date);
                    // Si dimanche ferme sans horaire: on enregistre un "CONGE" de repos hebdomadaire
                    // pour ne pas penaliser artificiellement la semaine.
                    boolean weeklyRest = row.date.getDayOfWeek() == DayOfWeek.SUNDAY && !scheduleService.isStoreOpen(row.date);
                    boolean restAlreadyRecorded = existingAbsences.stream()
                        .anyMatch(absence -> row.date.equals(absence.getAbsenceDate()));
                    if (weeklyRest && !restAlreadyRecorded) {
                        absenceService.recordAbsence(badge, row.date, "CONGE", "Repos hebdomadaire");
                    }
                }
            }
            loadPlanningWeek();
            view.outputArea().append("\nPlanning manuel enregistre pour " + badge + " - semaine du "
                + selectedPlanningMonday() + "\n");
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void updateNeedsChart(LocalDate monday) {
        int minStaff = parseInt(view.minStaffField().getText().trim(), 3);
        String selectedBadge = view.selectedPlanningBadge();
        // "Autres employes" = shifts deja persistes, hors employe actuellement edite.
        List<WorkShift> persistedOtherShifts = scheduleService.getWeekSchedule(monday).stream()
            .filter(WorkShift::isActive)
            .filter(shift -> selectedBadge.isBlank() || !selectedBadge.equalsIgnoreCase(shift.getBadgeNumber()))
            .toList();
        List<NeedBar> bars = new ArrayList<>();
        for (int day = 0; day <= 6; day++) {
            LocalDate date = monday.plusDays(day);
            if (!scheduleService.isStoreOpen(date) && !hasManualNeedProfile(date)) {
                continue;
            }
            // Resolution au quart d'heure pour coller au pilotage fin des besoins.
            for (int minute = 8 * 60; minute < 20 * 60; minute += 15) {
                LocalTime slotStart = LocalTime.of(minute / 60, minute % 60);
                LocalTime slotEnd = slotStart.plusMinutes(15);
                int required = requiredStaffForChart(date, slotStart, minStaff);
                int planned = countPersistedCoverage(persistedOtherShifts, date, slotStart, slotEnd)
                    + countVisibleEmployeeCoverage(date, slotStart, slotEnd);
                int needed = required - planned;
                bars.add(new NeedBar(date, minute, needed, required, planned));
            }
        }
        view.needsChart().setBars(bars);
    }

    private void openNeedEditor() {
        try {
            LocalDate monday = selectedPlanningMonday();
            int minStaff = parseInt(view.minStaffField().getText().trim(), 3);
            List<NeedEditorSlot> slots = new ArrayList<>();
            int editorMax = Math.max(1, manualNeedsMax);
            for (int day = 0; day <= 6; day++) {
                LocalDate date = monday.plusDays(day);
                for (int minute = 8 * 60; minute < 20 * 60; minute += 15) {
                    LocalTime slotStart = LocalTime.of(minute / 60, minute % 60);
                    int required = requiredStaffForChart(date, slotStart, minStaff);
                    editorMax = Math.max(editorMax, required);
                    slots.add(new NeedEditorSlot(date, minute, required));
                }
            }
            view.showNeedEditor(slots, editorMax, (editedSlots, maxValue) -> {
                manualNeedsMax = Math.max(1, maxValue);
                for (NeedEditorSlot slot : editedSlots) {
                    manualNeeds.put(needKey(slot.date, slot.minuteOfDay), Math.max(0, Math.min(manualNeedsMax, slot.value)));
                }
                updateNeedsChart(monday);
                view.outputArea().append("\nGraphe de besoin mis a jour pour la semaine du " + monday + "\n");
            });
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private int requiredStaffForChart(LocalDate date, LocalTime slotStart, int minStaff) {
        String key = needKey(date, slotStart.getHour() * 60 + slotStart.getMinute());
        Integer manualValue = manualNeeds.get(key);
        // Le graphe manuel prime sur l'estimation automatique quand il existe.
        if (manualValue != null) {
            return manualValue;
        }
        return scheduleService.requiredStaffForSlot(date, slotStart, minStaff);
    }

    private boolean hasManualNeedProfile(LocalDate date) {
        String prefix = date + "#";
        return manualNeeds.keySet().stream().anyMatch(key -> key.startsWith(prefix));
    }

    private String needKey(LocalDate date, int minuteOfDay) {
        return date + "#" + minuteOfDay;
    }

    private int countPersistedCoverage(List<WorkShift> shifts, LocalDate date, LocalTime slotStart, LocalTime slotEnd) {
        return (int) shifts.stream()
            .filter(shift -> date.equals(shift.getShiftDate()))
            .filter(shift -> overlaps(shift.getStartTime(), shift.getEndTime(), slotStart, slotEnd))
            .count();
    }

    private int countVisibleEmployeeCoverage(LocalDate date, LocalTime slotStart, LocalTime slotEnd) {
        for (ShiftRow row : view.planningTableModel().rows()) {
            if (!date.equals(row.date) || !row.storeOpen || row.absence || !row.hasShiftInput()) {
                continue;
            }
            try {
                if (overlaps(parseTime(row.startText), parseTime(row.endText), slotStart, slotEnd)) {
                    return 1;
                }
            } catch (DateTimeParseException ignored) {
                return 0;
            }
        }
        return 0;
    }

    private boolean overlaps(LocalTime start, LocalTime end, LocalTime slotStart, LocalTime slotEnd) {
        return start != null && end != null && start.isBefore(slotEnd) && end.isAfter(slotStart);
    }

    private void syncPlanningSelector(LocalDate anyDateInWeek) {
        int year = scheduleService.isoWeekYear(anyDateInWeek);
        int week = scheduleService.isoWeekNumber(anyDateInWeek);
        planningRefreshInProgress = true;
        view.planningYearField().setText(Integer.toString(year));
        view.setPlanningWeeks(scheduleService.weeksInYear(year), week);
        planningRefreshInProgress = false;
        view.weekStartField().setText(scheduleService.mondayOfIsoWeek(year, week).toString());
    }

    private LocalDate selectedPlanningMonday() {
        int year = parseInt(view.planningYearField().getText().trim(), LocalDate.now().getYear());
        Integer selectedWeek = (Integer) view.planningWeekCombo().getSelectedItem();
        int week = selectedWeek == null ? 1 : selectedWeek;
        return scheduleService.mondayOfIsoWeek(year, week);
    }

    private void exportSchedulePdfAll() {
        exportSchedulePdf(null);
    }

    private void exportSchedulePdfByBadge() {
        exportSchedulePdf(view.exportBadgeField().getText().trim());
    }

    private void exportSchedulePdf(String badge) {
        try {
            LocalDate weekStart = LocalDate.parse(view.weekStartField().getText().trim());
            String path = scheduleService.exportWeekToPdf(weekStart, badge);
            JOptionPane.showMessageDialog(view, "PDF exporte: " + path, "Export PDF", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void runForecast() {
        try {
            int months = parseInt(view.forecastMonthsField().getText().trim(), 3);
            int minStaff = parseInt(view.minStaffField().getText().trim(), 3);
            List<String> alerts = scheduleService.forecastStaffingAlerts(months, minStaff);
            StringBuilder sb = new StringBuilder(view.outputArea().getText());
            sb.append("\n\nALERTES EFFECTIF\n");
            alerts.forEach(alert -> sb.append("- ").append(alert).append('\n'));
            view.outputArea().setText(sb.toString());
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private LocalTime parseTime(String text) {
        return LocalTime.parse(text.trim());
    }

    private String dayLabel(DayOfWeek day) {
        return switch (day) {
            case MONDAY -> "Lundi";
            case TUESDAY -> "Mardi";
            case WEDNESDAY -> "Mercredi";
            case THURSDAY -> "Jeudi";
            case FRIDAY -> "Vendredi";
            case SATURDAY -> "Samedi";
            case SUNDAY -> "Dimanche";
        };
    }

    private String formatHours(double hours) {
        return String.format(Locale.US, "%.2f", hours);
    }

    private int parseInt(String text, int fallback) {
        try {
            return Integer.parseInt(text);
        } catch (Exception ex) {
            return fallback;
        }
    }

    private double parseDouble(String text, double fallback) {
        try {
            return Double.parseDouble(text.replace(',', '.'));
        } catch (Exception ex) {
            return fallback;
        }
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(view, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
    }
}
