package com.leadelmarche.domain.people;

import com.leadelmarche.common.Address;
import com.leadelmarche.common.BaseEntity;
import java.time.LocalDateTime;

public class Employee extends BaseEntity {
    private String badgeNumber;
    private String firstName;
    private String lastName;
    private Address homeAddress;
    private Address workAddress;
    private String role;
    private String supervisorBadge;

    public Employee() {
        super();
        this.homeAddress = new Address();
        this.workAddress = new Address();
    }

    public Employee(String id, boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(id, active, createdAt, updatedAt);
        this.homeAddress = new Address();
        this.workAddress = new Address();
    }

    public String getBadgeNumber() {
        return badgeNumber;
    }

    public void setBadgeNumber(String badgeNumber) {
        this.badgeNumber = badgeNumber;
        touch();
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
        touch();
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
        touch();
    }

    public Address getHomeAddress() {
        return homeAddress;
    }

    public void setHomeAddress(Address homeAddress) {
        this.homeAddress = homeAddress;
        touch();
    }

    public Address getWorkAddress() {
        return workAddress;
    }

    public void setWorkAddress(Address workAddress) {
        this.workAddress = workAddress;
        touch();
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
        touch();
    }

    public String getSupervisorBadge() {
        return supervisorBadge;
    }

    public void setSupervisorBadge(String supervisorBadge) {
        this.supervisorBadge = supervisorBadge;
        touch();
    }

    public String fullName() {
        return (firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName);
    }
}

