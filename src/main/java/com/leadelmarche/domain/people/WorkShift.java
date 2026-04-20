package com.leadelmarche.domain.people;

import com.leadelmarche.common.BaseEntity;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class WorkShift extends BaseEntity {
    private String employeeId;
    private String badgeNumber;
    private String employeeName;
    private LocalDate shiftDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal paidMultiplier;
    private String breakPlan;
    private String weekId;

    public WorkShift() {
        super();
        this.employeeId = "";
        this.badgeNumber = "";
        this.employeeName = "";
        this.shiftDate = LocalDate.now();
        this.startTime = LocalTime.of(8, 0);
        this.endTime = LocalTime.of(16, 0);
        this.paidMultiplier = BigDecimal.ONE;
        this.breakPlan = "";
        this.weekId = "";
    }

    public WorkShift(String id, boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(id, active, createdAt, updatedAt);
        this.employeeId = "";
        this.badgeNumber = "";
        this.employeeName = "";
        this.shiftDate = LocalDate.now();
        this.startTime = LocalTime.of(8, 0);
        this.endTime = LocalTime.of(16, 0);
        this.paidMultiplier = BigDecimal.ONE;
        this.breakPlan = "";
        this.weekId = "";
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
        touch();
    }

    public String getBadgeNumber() {
        return badgeNumber;
    }

    public void setBadgeNumber(String badgeNumber) {
        this.badgeNumber = badgeNumber;
        touch();
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
        touch();
    }

    public LocalDate getShiftDate() {
        return shiftDate;
    }

    public void setShiftDate(LocalDate shiftDate) {
        this.shiftDate = shiftDate;
        touch();
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
        touch();
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
        touch();
    }

    public BigDecimal getPaidMultiplier() {
        return paidMultiplier;
    }

    public void setPaidMultiplier(BigDecimal paidMultiplier) {
        this.paidMultiplier = paidMultiplier;
        touch();
    }

    public String getBreakPlan() {
        return breakPlan;
    }

    public void setBreakPlan(String breakPlan) {
        this.breakPlan = breakPlan;
        touch();
    }

    public String getWeekId() {
        return weekId;
    }

    public void setWeekId(String weekId) {
        this.weekId = weekId;
        touch();
    }
}
