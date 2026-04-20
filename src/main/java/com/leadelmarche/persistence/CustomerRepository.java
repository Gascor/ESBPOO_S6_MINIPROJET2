package com.leadelmarche.persistence;

import com.leadelmarche.domain.customer.Customer;
import java.util.List;
import java.util.Optional;

public class CustomerRepository extends AbstractTextRepository<Customer> {
    public CustomerRepository(TextFileDatabase database) {
        super(database, "customers.txt");
    }

    @Override
    protected Customer fromFields(List<String> fields) {
        Customer customer = new Customer();
        int offset = readBaseFields(customer, fields);
        customer.setFirstName(field(fields, offset++));
        customer.setLastName(field(fields, offset++));
        customer.setLoyaltyCardNumber(field(fields, offset++));
        customer.setEmail(field(fields, offset++));
        customer.setPostalCode(field(fields, offset++));
        customer.setAnonymous(Boolean.parseBoolean(field(fields, offset)));
        return customer;
    }

    @Override
    protected List<String> toFields(Customer entity) {
        List<String> fields = withBaseFields(entity);
        fields.add(entity.getFirstName());
        fields.add(entity.getLastName());
        fields.add(entity.getLoyaltyCardNumber());
        fields.add(entity.getEmail());
        fields.add(entity.getPostalCode());
        fields.add(Boolean.toString(entity.isAnonymous()));
        return fields;
    }

    @Override
    protected String nameOf(Customer entity) {
        return entity.fullName() + " " + entity.getLoyaltyCardNumber();
    }

    public Optional<Customer> findByCardNumber(String cardNumber) {
        return findAll(true).stream()
            .filter(c -> c.getLoyaltyCardNumber() != null && c.getLoyaltyCardNumber().equalsIgnoreCase(cardNumber))
            .findFirst();
    }
}

