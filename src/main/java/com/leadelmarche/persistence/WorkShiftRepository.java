package com.leadelmarche.persistence;

import com.leadelmarche.domain.people.WorkShift;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class WorkShiftRepository extends AbstractTextRepository<WorkShift> {
    public WorkShiftRepository(TextFileDatabase database) {
        super(database, "work_shifts.txt");
    }

    @Override
    protected WorkShift fromFields(List<String> fields) {
        WorkShift shift = new WorkShift();
        int offset = readBaseFields(shift, fields);
        shift.setEmployeeId(field(fields, offset++));
        shift.setBadgeNumber(field(fields, offset++));
        shift.setEmployeeName(field(fields, offset++));
        shift.setShiftDate(parseDate(field(fields, offset++)));
        shift.setStartTime(parseTime(field(fields, offset++)));
        shift.setEndTime(parseTime(field(fields, offset++)));
        shift.setPaidMultiplier(parseBigDecimalSafe(field(fields, offset++)));
        shift.setBreakPlan(field(fields, offset++));
        shift.setWeekId(field(fields, offset));
        return shift;
    }

    @Override
    protected List<String> toFields(WorkShift entity) {
        List<String> fields = withBaseFields(entity);
        fields.add(entity.getEmployeeId());
        fields.add(entity.getBadgeNumber());
        fields.add(entity.getEmployeeName());
        fields.add(entity.getShiftDate().toString());
        fields.add(entity.getStartTime().toString());
        fields.add(entity.getEndTime().toString());
        fields.add(entity.getPaidMultiplier().toPlainString());
        fields.add(entity.getBreakPlan());
        fields.add(entity.getWeekId());
        return fields;
    }

    @Override
    protected String nameOf(WorkShift entity) {
        return entity.getBadgeNumber() + " " + entity.getShiftDate();
    }

    public List<WorkShift> findByWeekId(String weekId) {
        return findAll(true).stream()
            .filter(s -> weekId.equals(s.getWeekId()))
            .toList();
    }

    private LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text);
        } catch (Exception ex) {
            return LocalDate.now();
        }
    }

    private LocalTime parseTime(String text) {
        try {
            return LocalTime.parse(text);
        } catch (Exception ex) {
            return LocalTime.of(8, 0);
        }
    }

    private BigDecimal parseBigDecimalSafe(String text) {
        try {
            return new BigDecimal(text);
        } catch (Exception ex) {
            return BigDecimal.ONE;
        }
    }
}
