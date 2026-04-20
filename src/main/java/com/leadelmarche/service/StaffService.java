package com.leadelmarche.service;

import com.leadelmarche.domain.people.Employee;
import com.leadelmarche.persistence.EmployeeRepository;
import java.util.List;
import java.util.Optional;

public class StaffService {
    private final EmployeeRepository employeeRepository;

    public StaffService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public Employee createEmployee(Employee employee) {
        return employeeRepository.create(employee);
    }

    public Employee updateEmployee(Employee employee) {
        return employeeRepository.update(employee);
    }

    public void deactivateEmployee(String employeeId) {
        employeeRepository.findById(employeeId).ifPresent(e -> {
            e.setActive(false);
            employeeRepository.update(e);
        });
    }

    public List<Employee> listEmployees(boolean activeOnly) {
        return employeeRepository.findAll(activeOnly);
    }

    public List<Employee> searchEmployees(String partial) {
        return employeeRepository.searchByName(partial);
    }

    public Optional<Employee> findByBadge(String badgeNumber) {
        return employeeRepository.findByBadge(badgeNumber);
    }

    public Optional<Employee> findById(String employeeId) {
        return employeeRepository.findById(employeeId);
    }
}
