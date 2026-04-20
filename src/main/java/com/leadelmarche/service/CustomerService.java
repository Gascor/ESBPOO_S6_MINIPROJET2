package com.leadelmarche.service;

import com.leadelmarche.domain.customer.Customer;
import com.leadelmarche.persistence.CustomerRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class CustomerService {
    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer createCustomer(Customer customer) {
        return customerRepository.create(customer);
    }

    public Customer updateCustomer(Customer customer) {
        return customerRepository.update(customer);
    }

    public void deactivateCustomer(String customerId) {
        customerRepository.findById(customerId).ifPresent(c -> {
            // DELETE logique: permet de conserver l'historique des ventes liées au client.
            c.setActive(false);
            customerRepository.update(c);
        });
    }

    public List<Customer> listCustomers(boolean activeOnly) {
        return customerRepository.findAll(activeOnly);
    }

    public List<Customer> searchCustomers(String partial) {
        return customerRepository.searchByName(partial);
    }

    public Optional<Customer> getByCardNumber(String cardNumber) {
        return customerRepository.findByCardNumber(cardNumber);
    }

    public Optional<Customer> getById(String customerId) {
        return customerRepository.findById(customerId);
    }

    public void ensureAnonymousCustomerExists() {
        // Le client "Anonyme" est requis pour les ventes sans carte fidelite.
        if (customerRepository.findByCardNumber(Customer.ANONYMOUS_CARD).isPresent()) {
            return;
        }
        Customer anonymous = new Customer();
        anonymous.setFirstName("Client");
        anonymous.setLastName("Anonyme");
        anonymous.setLoyaltyCardNumber(Customer.ANONYMOUS_CARD);
        anonymous.setEmail("");
        anonymous.setPostalCode("");
        anonymous.setAnonymous(true);
        customerRepository.create(anonymous);
    }

    public Customer findOrAnonymous(String cardNumber) {
        ensureAnonymousCustomerExists();
        if (cardNumber != null && !cardNumber.isBlank()) {
            // Si la carte existe, on utilise le vrai client; sinon fallback anonyme.
            Optional<Customer> customer = customerRepository.findByCardNumber(cardNumber);
            if (customer.isPresent()) {
                return customer.get();
            }
        }
        return customerRepository.findByCardNumber(Customer.ANONYMOUS_CARD).orElseThrow(
            () -> new IllegalStateException("Anonymous customer missing")
        );
    }

    public void creditLoyalty(String cardNumber, BigDecimal amount) {
        // MVP: la cagnotte est tracée dans les ventes/tickets.
        // Extension prévue: brancher un vrai repository LoyaltyAccount sans casser l'API service.
    }
}
