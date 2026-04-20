package com.leadelmarche.persistence;

import com.leadelmarche.domain.people.StaffAbsence;
import java.time.LocalDate;
import java.util.List;

public class AbsenceRepository extends AbstractTextRepository<StaffAbsence> {
    public AbsenceRepository(TextFileDatabase database) {
        super(database, "absences.txt");
    }

    @Override
    protected StaffAbsence fromFields(List<String> fields) {
        StaffAbsence absence = new StaffAbsence();
        int offset = readBaseFields(absence, fields);
        absence.setBadgeNumber(field(fields, offset++));
        absence.setAbsenceDate(parseDate(field(fields, offset++)));
        absence.setType(field(fields, offset++));
        absence.setNote(field(fields, offset));
        return absence;
    }

    @Override
    protected List<String> toFields(StaffAbsence entity) {
        List<String> fields = withBaseFields(entity);
        fields.add(entity.getBadgeNumber());
        fields.add(entity.getAbsenceDate().toString());
        fields.add(entity.getType());
        fields.add(entity.getNote());
        return fields;
    }

    @Override
    protected String nameOf(StaffAbsence entity) {
        return entity.getBadgeNumber() + " " + entity.getType() + " " + entity.getAbsenceDate();
    }

    private LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text);
        } catch (Exception ex) {
            return LocalDate.now();
        }
    }
}
