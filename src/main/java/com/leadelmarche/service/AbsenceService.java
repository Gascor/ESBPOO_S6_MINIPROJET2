package com.leadelmarche.service;

import com.leadelmarche.domain.people.StaffAbsence;
import com.leadelmarche.persistence.AbsenceRepository;
import java.time.LocalDate;
import java.util.List;

public class AbsenceService {
    private final AbsenceRepository absenceRepository;

    public AbsenceService(AbsenceRepository absenceRepository) {
        this.absenceRepository = absenceRepository;
    }

    public StaffAbsence recordAbsence(String badgeNumber, LocalDate date, String type, String note) {
        if (badgeNumber == null || badgeNumber.isBlank()) {
            throw new IllegalArgumentException("Badge obligatoire");
        }
        StaffAbsence absence = new StaffAbsence();
        absence.setBadgeNumber(badgeNumber.trim());
        absence.setAbsenceDate(date == null ? LocalDate.now() : date);
        absence.setType(type == null || type.isBlank() ? "ABSENCE" : type.trim().toUpperCase());
        absence.setNote(note == null ? "" : note.trim());
        return absenceRepository.create(absence);
    }

    public List<StaffAbsence> listAbsences(boolean activeOnly) {
        return absenceRepository.findAll(activeOnly);
    }

    public void deactivateAbsence(String absenceId) {
        absenceRepository.findById(absenceId).ifPresent(absence -> {
            absence.setActive(false);
            absenceRepository.update(absence);
        });
    }
}
