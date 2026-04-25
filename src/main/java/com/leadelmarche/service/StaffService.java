package com.leadelmarche.service;

import com.leadelmarche.domain.people.Employee;
import com.leadelmarche.domain.people.ContractType;
import com.leadelmarche.persistence.EmployeeRepository;
import java.util.List;
import java.util.Optional;

public class StaffService {
    private final EmployeeRepository employeeRepository;

    public StaffService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public Employee createEmployee(Employee employee) {
        validateContract(employee);
        return employeeRepository.create(employee);
    }

    public Employee updateEmployee(Employee employee) {
        validateContract(employee);
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

    private void validateContract(Employee employee) {
        if (employee == null) {
            throw new IllegalArgumentException("Employe obligatoire");
        }
        if (employee.getBadgeNumber() == null || employee.getBadgeNumber().isBlank()) {
            throw new IllegalArgumentException("Badge employe obligatoire");
        }
        if (employee.getContractType() == null) {
            employee.setContractType(ContractType.CDI_35H);
        }
        if (employee.getContractType() == ContractType.CDI_35H) {
            employee.setContractWeeklyHours(35);
            return;
        }
        int hours = employee.getContractWeeklyHours();
        if (employee.getContractType() == ContractType.CDI_ETUDIANT && (hours < 7 || hours > 21)) {
            throw new IllegalArgumentException("Un CDI etudiant doit etre entre 7h et 21h par semaine");
        }
        if ((employee.getContractType() == ContractType.CDD || employee.getContractType() == ContractType.INTERIM) && hours <= 0) {
            throw new IllegalArgumentException("Un CDD/interimaire doit avoir un volume horaire positif");
        }
    }
}
