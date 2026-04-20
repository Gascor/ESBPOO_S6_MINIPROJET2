package com.leadelmarche.ui.mvc.controller;

import com.leadelmarche.common.Address;
import com.leadelmarche.domain.people.Employee;
import com.leadelmarche.service.StaffService;
import com.leadelmarche.ui.mvc.view.StaffView;
import java.util.List;
import javax.swing.JOptionPane;

public class StaffController {
    private final StaffService staffService;
    private final StaffView view;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
        this.view = new StaffView();
        bindActions();
        refreshEmployees();
    }

    public void show() {
        view.setVisible(true);
    }

    private void bindActions() {
        view.addButton().addActionListener(e -> addEmployee());
        view.searchButton().addActionListener(e -> searchEmployees());
        view.refreshButton().addActionListener(e -> refreshEmployees());
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

    private void render(List<Employee> employees) {
        StringBuilder sb = new StringBuilder();
        sb.append("ID | BADGE | NOM | PRENOM | ROLE | SUP\n");
        sb.append("---------------------------------------------------------------\n");
        for (Employee e : employees) {
            sb.append(e.getId()).append(" | ")
                .append(e.getBadgeNumber()).append(" | ")
                .append(e.getLastName()).append(" | ")
                .append(e.getFirstName()).append(" | ")
                .append(e.getRole()).append(" | ")
                .append(e.getSupervisorBadge()).append('\n');
        }
        view.outputArea().setText(sb.toString());
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(view, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
    }
}

