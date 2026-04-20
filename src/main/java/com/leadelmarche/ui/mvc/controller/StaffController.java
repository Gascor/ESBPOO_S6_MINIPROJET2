package com.leadelmarche.ui.mvc.controller;

import com.leadelmarche.common.Address;
import com.leadelmarche.domain.people.ContractType;
import com.leadelmarche.domain.people.Employee;
import com.leadelmarche.domain.people.WorkShift;
import com.leadelmarche.service.AbsenceService;
import com.leadelmarche.service.ScheduleService;
import com.leadelmarche.service.StaffService;
import com.leadelmarche.ui.mvc.view.StaffView;
import java.time.LocalDate;
import java.util.List;
import javax.swing.JOptionPane;

public class StaffController {
    private final StaffService staffService;
    private final AbsenceService absenceService;
    private final ScheduleService scheduleService;
    private final StaffView view;

    public StaffController(StaffService staffService, AbsenceService absenceService, ScheduleService scheduleService) {
        this.staffService = staffService;
        this.absenceService = absenceService;
        this.scheduleService = scheduleService;
        this.view = new StaffView();
        bindActions();
        refreshEmployees();
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
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void searchEmployees() {
        render(staffService.searchEmployees(view.searchField().getText()));
    }

    private void refreshEmployees() {
        render(staffService.listEmployees(true));
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
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void generateSchedule() {
        try {
            LocalDate weekStart = LocalDate.parse(view.weekStartField().getText().trim());
            int minStaff = parseInt(view.minStaffField().getText().trim(), 3);
            List<WorkShift> shifts = scheduleService.generateWeeklySchedule(weekStart, minStaff);
            StringBuilder sb = new StringBuilder(view.outputArea().getText());
            sb.append("\n\nPLANNING GENERE (semaine ").append(weekStart).append(")\n");
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

    private int parseInt(String text, int fallback) {
        try {
            return Integer.parseInt(text);
        } catch (Exception ex) {
            return fallback;
        }
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(view, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
    }
}
