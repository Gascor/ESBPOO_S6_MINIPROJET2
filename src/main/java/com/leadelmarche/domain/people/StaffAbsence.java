package com.leadelmarche.domain.people;

import com.leadelmarche.common.BaseEntity;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class StaffAbsence extends BaseEntity {
    private String badgeNumber;
    private LocalDate absenceDate;
    private String type;
    private String note;

    public StaffAbsence() {
        super();
        this.absenceDate = LocalDate.now();
        this.type = "ABSENCE";
        this.note = "";
    }

    public StaffAbsence(String id, boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(id, active, createdAt, updatedAt);
        this.absenceDate = LocalDate.now();
        this.type = "ABSENCE";
        this.note = "";
    }

    public String getBadgeNumber() {
        return badgeNumber;
    }

    public void setBadgeNumber(String badgeNumber) {
        this.badgeNumber = badgeNumber;
        touch();
    }

    public LocalDate getAbsenceDate() {
        return absenceDate;
    }

    public void setAbsenceDate(LocalDate absenceDate) {
        this.absenceDate = absenceDate;
        touch();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
        touch();
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
        touch();
    }
}
