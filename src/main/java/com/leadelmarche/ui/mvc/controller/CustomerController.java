package com.leadelmarche.ui.mvc.controller;

import com.leadelmarche.domain.customer.Customer;
import com.leadelmarche.service.CustomerService;
import com.leadelmarche.ui.mvc.view.CustomerView;
import java.util.List;
import javax.swing.JOptionPane;

public class CustomerController {
    private final CustomerService customerService;
    private final CustomerView view;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
        this.view = new CustomerView();
        bindActions();
        refreshCustomers();
    }

    public void show() {
        view.setVisible(true);
    }

    private void bindActions() {
        view.addButton().addActionListener(e -> addCustomer());
        view.updateButton().addActionListener(e -> updateCustomer());
        view.deactivateButton().addActionListener(e -> deactivateCustomer());
        view.searchButton().addActionListener(e -> searchCustomers());
        view.refreshButton().addActionListener(e -> refreshCustomers());
    }

    private void addCustomer() {
        try {
            Customer customer = new Customer();
            customer.setFirstName(view.firstNameField().getText().trim());
            customer.setLastName(view.lastNameField().getText().trim());
            customer.setLoyaltyCardNumber(view.cardField().getText().trim());
            customer.setEmail(view.emailField().getText().trim());
            customer.setPostalCode(view.postalField().getText().trim());
            customer.setAnonymous(false);
            customerService.createCustomer(customer);
            refreshCustomers();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void searchCustomers() {
        render(customerService.searchCustomers(view.searchField().getText()));
    }

    private void updateCustomer() {
        try {
            String customerId = view.editCustomerIdField().getText().trim();
            if (customerId.isBlank()) {
                throw new IllegalArgumentException("ID client obligatoire pour la mise a jour");
            }
            Customer customer = customerService.getById(customerId).orElseThrow(
                () -> new IllegalStateException("Client introuvable: " + customerId)
            );
            if (!view.firstNameField().getText().trim().isBlank()) {
                customer.setFirstName(view.firstNameField().getText().trim());
            }
            if (!view.lastNameField().getText().trim().isBlank()) {
                customer.setLastName(view.lastNameField().getText().trim());
            }
            if (!view.cardField().getText().trim().isBlank()) {
                customer.setLoyaltyCardNumber(view.cardField().getText().trim());
            }
            if (!view.emailField().getText().trim().isBlank()) {
                customer.setEmail(view.emailField().getText().trim());
            }
            if (!view.postalField().getText().trim().isBlank()) {
                customer.setPostalCode(view.postalField().getText().trim());
            }
            customerService.updateCustomer(customer);
            refreshCustomers();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void deactivateCustomer() {
        try {
            String customerId = view.editCustomerIdField().getText().trim();
            if (customerId.isBlank()) {
                throw new IllegalArgumentException("ID client obligatoire pour la desactivation");
            }
            Customer customer = customerService.getById(customerId).orElseThrow(
                () -> new IllegalStateException("Client introuvable: " + customerId)
            );
            if (customer.isAnonymous()) {
                throw new IllegalStateException("Le client Anonyme ne peut pas etre desactive.");
            }
            customerService.deactivateCustomer(customerId);
            refreshCustomers();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void refreshCustomers() {
        render(customerService.listCustomers(true));
    }

    private void render(List<Customer> customers) {
        StringBuilder sb = new StringBuilder();
        sb.append("ID | NOM | PRENOM | CARTE | EMAIL | CP\n");
        sb.append("---------------------------------------------------------------\n");
        for (Customer c : customers) {
            sb.append(c.getId()).append(" | ")
                .append(c.getLastName()).append(" | ")
                .append(c.getFirstName()).append(" | ")
                .append(c.getLoyaltyCardNumber()).append(" | ")
                .append(c.getEmail()).append(" | ")
                .append(c.getPostalCode()).append('\n');
        }
        view.outputArea().setText(sb.toString());
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(view, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
    }
}
