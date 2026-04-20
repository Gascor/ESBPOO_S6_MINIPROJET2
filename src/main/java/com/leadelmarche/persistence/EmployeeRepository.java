package com.leadelmarche.persistence;

import com.leadelmarche.common.Address;
import com.leadelmarche.domain.people.Employee;
import java.util.List;
import java.util.Optional;

public class EmployeeRepository extends AbstractTextRepository<Employee> {
    public EmployeeRepository(TextFileDatabase database) {
        super(database, "employees.txt");
    }

    @Override
    protected Employee fromFields(List<String> fields) {
        Employee employee = new Employee();
        int offset = readBaseFields(employee, fields);
        employee.setBadgeNumber(field(fields, offset++));
        employee.setFirstName(field(fields, offset++));
        employee.setLastName(field(fields, offset++));
        employee.setHomeAddress(parseAddress(field(fields, offset++)));
        employee.setWorkAddress(parseAddress(field(fields, offset++)));
        employee.setRole(field(fields, offset++));
        employee.setSupervisorBadge(field(fields, offset));
        return employee;
    }

    @Override
    protected List<String> toFields(Employee entity) {
        List<String> fields = withBaseFields(entity);
        fields.add(entity.getBadgeNumber());
        fields.add(entity.getFirstName());
        fields.add(entity.getLastName());
        fields.add(serializeAddress(entity.getHomeAddress()));
        fields.add(serializeAddress(entity.getWorkAddress()));
        fields.add(entity.getRole());
        fields.add(entity.getSupervisorBadge());
        return fields;
    }

    @Override
    protected String nameOf(Employee entity) {
        return entity.fullName() + " " + entity.getBadgeNumber();
    }

    public Optional<Employee> findByBadge(String badge) {
        return findAll(true).stream()
            .filter(e -> e.getBadgeNumber() != null && e.getBadgeNumber().equalsIgnoreCase(badge))
            .findFirst();
    }

    private String serializeAddress(Address address) {
        if (address == null) {
            return ";;;"; // line1;city;postal;country
        }
        return String.join(";", safe(address.getLine1()), safe(address.getCity()), safe(address.getPostalCode()), safe(address.getCountry()));
    }

    private Address parseAddress(String text) {
        String[] parts = text.split(";", -1);
        String line1 = parts.length > 0 ? parts[0] : "";
        String city = parts.length > 1 ? parts[1] : "";
        String postal = parts.length > 2 ? parts[2] : "";
        String country = parts.length > 3 ? parts[3] : "";
        return new Address(line1, city, postal, country);
    }

    private String safe(String value) {
        return value == null ? "" : value.replace(";", ",");
    }
}

