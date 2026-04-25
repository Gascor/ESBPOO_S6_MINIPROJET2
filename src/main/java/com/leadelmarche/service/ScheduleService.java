package com.leadelmarche.service;

import com.leadelmarche.domain.people.ContractType;
import com.leadelmarche.domain.people.Employee;
import com.leadelmarche.domain.people.StaffAbsence;
import com.leadelmarche.domain.people.WorkShift;
import com.leadelmarche.persistence.TextFileDatabase;
import com.leadelmarche.persistence.WorkShiftRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ScheduleService {
    private final StaffService staffService;
    private final AbsenceService absenceService;
    private final WorkShiftRepository workShiftRepository;
    private final SimplePdfService simplePdfService;
    private final TextFileDatabase database;

    public ScheduleService(
        StaffService staffService,
        AbsenceService absenceService,
        WorkShiftRepository workShiftRepository,
        SimplePdfService simplePdfService,
        TextFileDatabase database
    ) {
        this.staffService = staffService;
        this.absenceService = absenceService;
        this.workShiftRepository = workShiftRepository;
        this.simplePdfService = simplePdfService;
        this.database = database;
    }

    public List<WorkShift> generateWeeklySchedule(LocalDate anyDateInWeek, int minimumStaffPerDay) {
        LocalDate monday = normalizeToMonday(anyDateInWeek);
        String weekId = weekId(monday);
        List<WorkShift> existing = workShiftRepository.findByWeekId(weekId);
        for (WorkShift shift : existing) {
            shift.setActive(false);
            workShiftRepository.update(shift);
        }

        List<Employee> employees = staffService.listEmployees(true);
        List<WorkShift> generated = new ArrayList<>();
        for (int i = 0; i < employees.size(); i++) {
            Employee employee = employees.get(i);
            generated.addAll(buildEmployeeWeek(employee, monday, i, weekId));
        }
        generated.forEach(workShiftRepository::create);

        return workShiftRepository.findByWeekId(weekId).stream()
            .sorted(Comparator.comparing(WorkShift::getShiftDate).thenComparing(WorkShift::getBadgeNumber))
            .toList();
    }

    public WorkShift saveManualShift(String badgeNumber, LocalDate date, LocalTime start, LocalTime end) {
        Employee employee = staffService.findByBadge(badgeNumber).orElseThrow(
            () -> new IllegalArgumentException("Employe introuvable pour le badge " + badgeNumber)
        );
        if (hasAbsence(employee.getBadgeNumber(), date)) {
            throw new IllegalArgumentException("Impossible de planifier " + badgeNumber + " sur une absence/RTT/conge");
        }
        ShiftValidation validation = validateShift(employee, date, start, end);
        if (!validation.isValid()) {
            throw new IllegalArgumentException(validation.getMessage());
        }

        clearShift(employee.getBadgeNumber(), date);
        WorkShift shift = new WorkShift();
        shift.setEmployeeId(employee.getId());
        shift.setBadgeNumber(employee.getBadgeNumber());
        shift.setEmployeeName(employee.fullName().trim());
        shift.setShiftDate(date);
        shift.setStartTime(start);
        shift.setEndTime(end);
        shift.setPaidMultiplier(isDoublePaidSaleSunday(date) ? BigDecimal.valueOf(2) : BigDecimal.ONE);
        shift.setBreakPlan(validation.getBreakPlan());
        shift.setWeekId(weekId(normalizeToMonday(date)));
        return workShiftRepository.create(shift);
    }

    public void clearShift(String badgeNumber, LocalDate date) {
        if (badgeNumber == null || badgeNumber.isBlank() || date == null) {
            return;
        }
        String weekId = weekId(normalizeToMonday(date));
        workShiftRepository.findByWeekId(weekId).stream()
            .filter(shift -> badgeNumber.equalsIgnoreCase(shift.getBadgeNumber()))
            .filter(shift -> date.equals(shift.getShiftDate()))
            .forEach(shift -> {
                shift.setActive(false);
                workShiftRepository.update(shift);
            });
    }

    public List<WorkShift> getWeekSchedule(LocalDate anyDateInWeek) {
        LocalDate monday = normalizeToMonday(anyDateInWeek);
        return workShiftRepository.findByWeekId(weekId(monday)).stream()
            .sorted(Comparator.comparing(WorkShift::getShiftDate).thenComparing(WorkShift::getBadgeNumber))
            .toList();
    }

    public String exportWeekToPdf(LocalDate anyDateInWeek, String badgeFilter) {
        LocalDate monday = normalizeToMonday(anyDateInWeek);
        List<WorkShift> shifts = getWeekSchedule(monday).stream()
            .filter(shift -> shift.isActive())
            .filter(shift -> badgeFilter == null || badgeFilter.isBlank() || badgeFilter.equalsIgnoreCase(shift.getBadgeNumber()))
            .toList();
        List<String> lines = new ArrayList<>();
        lines.add("Semaine du " + monday + " au " + monday.plusDays(6));
        lines.add(" ");
        lines.add("Badge | Nom | Date | Debut | Fin | Pauses | Taux");
        lines.add("---------------------------------------------------------------------");
        for (WorkShift shift : shifts) {
            lines.add(
                shift.getBadgeNumber() + " | "
                    + shift.getEmployeeName() + " | "
                    + shift.getShiftDate() + " | "
                    + shift.getStartTime() + " | "
                    + shift.getEndTime() + " | "
                    + shift.getBreakPlan() + " | "
                    + shift.getPaidMultiplier()
            );
        }
        String suffix = (badgeFilter == null || badgeFilter.isBlank()) ? "all" : badgeFilter;
        String filename = "planning_" + monday + "_" + suffix + ".pdf";
        java.nio.file.Path path = database.getBasePath().resolve("exports").resolve(filename);
        simplePdfService.writeLines(path, "Planning LeadelMarche", lines);
        return path.toString();
    }

    public List<StaffingNeedSlot> estimateWeeklyNeeds(LocalDate anyDateInWeek, int minimumStaffPerHour) {
        LocalDate monday = normalizeToMonday(anyDateInWeek);
        List<WorkShift> shifts = getWeekSchedule(monday);
        List<StaffingNeedSlot> slots = new ArrayList<>();
        int base = Math.max(1, minimumStaffPerHour);
        for (int day = 0; day <= 6; day++) {
            LocalDate date = monday.plusDays(day);
            if (!isStoreOpen(date)) {
                continue;
            }
            for (int hour = 8; hour < 20; hour++) {
                int required = estimatedRequiredStaff(date, hour, base);
                int planned = plannedStaffAt(shifts, date, hour);
                slots.add(new StaffingNeedSlot(date, hour, required, planned));
            }
        }
        return slots;
    }

    public List<String> forecastStaffingAlerts(int monthsAhead, int minimumStaffPerDay) {
        int months = Math.max(1, monthsAhead);
        int targetPerDay = Math.max(1, minimumStaffPerDay);
        BigDecimal availableWeeklyHours = BigDecimal.valueOf(staffService.listEmployees(true).stream()
            .mapToInt(this::weeklyContractHours)
            .sum());
        BigDecimal requiredWeeklyHours = BigDecimal.valueOf(targetPerDay * 8L * 6L);
        List<String> alerts = new ArrayList<>();
        LocalDate base = LocalDate.now().withDayOfMonth(1);
        for (int i = 1; i <= months; i++) {
            LocalDate month = base.plusMonths(i);
            BigDecimal seasonalNeed = requiredWeeklyHours;
            List<String> reasons = new ArrayList<>();
            if (isSaleMonth(month)) {
                seasonalNeed = seasonalNeed.add(BigDecimal.valueOf(targetPerDay * 8L * 2L));
                reasons.add("soldes et 2 dimanches ouverts");
            }
            if (isSummerMonth(month)) {
                seasonalNeed = seasonalNeed.add(BigDecimal.valueOf(targetPerDay * 8L));
                reasons.add("remplacements vacances juillet/aout");
            }
            if (availableWeeklyHours.compareTo(seasonalNeed) < 0) {
                alerts.add(
                    "Alerte effectif " + month.getYear() + "-" + String.format("%02d", month.getMonthValue())
                        + ": capacite " + availableWeeklyHours + "h/semaine < besoin " + seasonalNeed
                        + "h/semaine"
                        + (reasons.isEmpty() ? "" : " (" + String.join(", ", reasons) + ")")
                        + ". Prevoir du personnel interimaire."
                );
            }
        }
        if (alerts.isEmpty()) {
            alerts.add("Pas d'alerte effectif sur les " + months + " prochains mois.");
        }
        return alerts;
    }

    public ShiftValidation validateShift(Employee employee, LocalDate date, LocalTime start, LocalTime end) {
        if (date == null) {
            return ShiftValidation.invalid("Date obligatoire");
        }
        if (start == null || end == null) {
            return ShiftValidation.rest("Repos");
        }
        if (!end.isAfter(start)) {
            return ShiftValidation.invalid("Heure de fin avant debut");
        }
        if (!isStoreOpen(date)) {
            return ShiftValidation.invalid("Magasin ferme ce jour");
        }
        LocalTime opening = LocalTime.of(8, 0);
        LocalTime closing = LocalTime.of(20, 0);
        if (start.isBefore(opening) || end.isAfter(closing)) {
            return ShiftValidation.invalid("Amplitude hors ouverture 08:00-20:00");
        }

        long grossMinutes = Duration.between(start, end).toMinutes();
        if (grossMinutes < 60) {
            return ShiftValidation.invalid("Amplitude trop courte");
        }

        if (grossMinutes < 7 * 60 + 15) {
            double paidHours = (grossMinutes - 15) / 60.0;
            if (paidHours <= 0) {
                return ShiftValidation.invalid("Pause impossible sur cette amplitude");
            }
            return ShiftValidation.valid("OK", "1x15min", paidHours);
        }
        if (grossMinutes < 8 * 60 + 30) {
            return ShiftValidation.invalid("Pause dejeuner obligatoire pour 7h et plus");
        }

        double paidHours = (grossMinutes - 90) / 60.0;
        if (paidHours > 8.0) {
            return ShiftValidation.invalid("Plus de 8h de travail effectif");
        }
        if (paidHours < 7.0) {
            return ShiftValidation.invalid("Amplitude incoherente avec pause dejeuner");
        }
        return ShiftValidation.valid("OK", "2x15min + dejeuner 1h", paidHours);
    }

    public boolean hasAbsence(String badgeNumber, LocalDate date) {
        if (badgeNumber == null || date == null) {
            return false;
        }
        return absenceService.listAbsences(true).stream()
            .anyMatch(a -> badgeNumber.equalsIgnoreCase(a.getBadgeNumber()) && date.equals(a.getAbsenceDate()));
    }

    public int contractWeeklyHours(Employee employee) {
        return weeklyContractHours(employee);
    }

    public boolean isStoreOpen(LocalDate date) {
        if (date == null) {
            return false;
        }
        if (isPaidClosedHoliday(date)) {
            return false;
        }
        return date.getDayOfWeek() != DayOfWeek.SUNDAY || isDoublePaidSaleSunday(date);
    }

    public boolean isPaidClosedHoliday(LocalDate date) {
        return isFixedClosedHoliday(date);
    }

    public double paidClosedHolidayCreditHours(int contractWeeklyHours) {
        int safeContractHours = Math.max(0, contractWeeklyHours);
        return safeContractHours / 5.0;
    }

    public int isoWeekNumber(LocalDate date) {
        return date.get(WeekFields.ISO.weekOfWeekBasedYear());
    }

    public int isoWeekYear(LocalDate date) {
        return date.get(WeekFields.ISO.weekBasedYear());
    }

    public int weeksInYear(int year) {
        return LocalDate.of(year, 12, 28).get(WeekFields.ISO.weekOfWeekBasedYear());
    }

    public int requiredStaffForSlot(LocalDate date, LocalTime slotStart, int minimumStaffPerHour) {
        if (date == null || slotStart == null || !isStoreOpen(date)) {
            return 0;
        }
        return estimatedRequiredStaff(date, slotStart.getHour(), Math.max(1, minimumStaffPerHour));
    }

    public LocalDate mondayOfIsoWeek(int year, int week) {
        int safeWeek = Math.max(1, Math.min(week, weeksInYear(year)));
        WeekFields iso = WeekFields.ISO;
        return LocalDate.of(year, 1, 4)
            .with(iso.weekBasedYear(), year)
            .with(iso.weekOfWeekBasedYear(), safeWeek)
            .with(iso.dayOfWeek(), 1);
    }

    private List<WorkShift> buildEmployeeWeek(Employee employee, LocalDate monday, int employeeIndex, String weekId) {
        int weeklyHours = weeklyContractHours(employee);
        double totalPaidHolidayCredit = 0;
        for (int day = 0; day <= 6; day++) {
            LocalDate date = monday.plusDays(day);
            if (isPaidClosedHoliday(date)) {
                totalPaidHolidayCredit += paidClosedHolidayCreditHours(weeklyHours);
            }
        }
        int plannedWeeklyHours = Math.max(0, (int) Math.round(weeklyHours - totalPaidHolidayCredit));
        if (plannedWeeklyHours <= 0) {
            return List.of();
        }
        Set<LocalDate> absences = absenceService.listAbsences(true).stream()
            .filter(a -> employee.getBadgeNumber().equalsIgnoreCase(a.getBadgeNumber()))
            .map(StaffAbsence::getAbsenceDate)
            .collect(Collectors.toSet());

        List<LocalDate> openDates = new ArrayList<>();
        for (int day = 0; day <= 6; day++) {
            LocalDate date = monday.plusDays(day);
            if (isStoreOpen(date)) {
                openDates.add(date);
            }
        }
        List<LocalDate> availableDates = openDates.stream()
            .filter(date -> !absences.contains(date))
            .toList();
        if (availableDates.isEmpty()) {
            return List.of();
        }

        int daysToWork = targetWorkingDays(plannedWeeklyHours, availableDates.size());
        List<LocalDate> selectedDates = rotateAndLimit(availableDates, employeeIndex, daysToWork);
        int remaining = plannedWeeklyHours;
        List<WorkShift> shifts = new ArrayList<>();
        for (int idx = 0; idx < selectedDates.size() && remaining > 0; idx++) {
            LocalDate date = selectedDates.get(idx);
            int daysLeft = selectedDates.size() - idx;
            int plannedHours = Math.min(8, (int) Math.ceil((double) remaining / daysLeft));
            plannedHours = Math.max(Math.min(4, remaining), plannedHours);
            plannedHours = Math.min(plannedHours, remaining);

            LocalTime start = suggestedStartTime(date, employeeIndex, idx);
            double span = plannedHours + breakDurationHours(plannedHours);
            LocalTime end = start.plusMinutes((long) (span * 60));
            if (end.isAfter(LocalTime.of(20, 0))) {
                end = LocalTime.of(20, 0);
                start = end.minusMinutes((long) (span * 60));
            }

            WorkShift shift = new WorkShift();
            shift.setEmployeeId(employee.getId());
            shift.setBadgeNumber(employee.getBadgeNumber());
            shift.setEmployeeName(employee.fullName().trim());
            shift.setShiftDate(date);
            shift.setStartTime(start);
            shift.setEndTime(end);
            shift.setPaidMultiplier(isDoublePaidSaleSunday(date) ? BigDecimal.valueOf(2) : BigDecimal.ONE);
            shift.setBreakPlan(buildBreakPlan(plannedHours));
            shift.setWeekId(weekId);
            shifts.add(shift);
            remaining -= plannedHours;
        }
        return shifts;
    }

    private int weeklyContractHours(Employee employee) {
        if (employee.getContractType() == ContractType.CDI_ETUDIANT) {
            int configured = employee.getContractWeeklyHours();
            if (configured < 7) {
                return 7;
            }
            return Math.min(configured, 21);
        }
        if (employee.getContractType() == ContractType.CDD || employee.getContractType() == ContractType.INTERIM) {
            return Math.max(7, employee.getContractWeeklyHours());
        }
        return 35;
    }

    private int targetWorkingDays(int weeklyHours, int availableDayCount) {
        if (availableDayCount <= 0) {
            return 0;
        }
        int target = Math.max(1, (int) Math.ceil(weeklyHours / 7.0));
        if (weeklyHours >= 35) {
            target = 5;
        }
        return Math.min(availableDayCount, target);
    }

    private List<LocalDate> rotateAndLimit(List<LocalDate> dates, int employeeIndex, int limit) {
        if (dates.isEmpty() || limit <= 0) {
            return List.of();
        }
        List<LocalDate> rotated = new ArrayList<>();
        int offset = employeeIndex % dates.size();
        for (int i = 0; i < dates.size(); i++) {
            rotated.add(dates.get((offset + i) % dates.size()));
        }
        return rotated.stream()
            .limit(limit)
            .sorted()
            .toList();
    }

    private LocalTime suggestedStartTime(LocalDate date, int employeeIndex, int dayIndex) {
        int[] minutes = {8 * 60, 8 * 60 + 30, 9 * 60, 10 * 60, 10 * 60 + 30, 11 * 60};
        int selected = minutes[Math.floorMod(employeeIndex + dayIndex, minutes.length)];
        if (date.getDayOfWeek() == DayOfWeek.SATURDAY || isSaleMonth(date) || isSummerMonth(date)) {
            selected += 30;
        }
        return LocalTime.of(selected / 60, selected % 60);
    }

    private double breakDurationHours(int plannedHours) {
        if (plannedHours >= 7) {
            return 1.5; // 1h lunch + 2x15m
        }
        return 0.25; // 1x15m
    }

    private String buildBreakPlan(int plannedHours) {
        if (plannedHours >= 7) {
            return "2x15min + dejeuner 1h";
        }
        return "1x15min";
    }

    private boolean isSaleMonth(LocalDate date) {
        int month = date.getMonthValue();
        return month == 1 || month == 7;
    }

    private boolean isSummerMonth(LocalDate date) {
        int month = date.getMonthValue();
        return month == 7 || month == 8;
    }

    private boolean isFixedClosedHoliday(LocalDate date) {
        int month = date.getMonthValue();
        int dayOfMonth = date.getDayOfMonth();
        return (month == 1 && dayOfMonth == 1)
            || (month == 5 && dayOfMonth == 1)
            || (month == 12 && dayOfMonth == 25);
    }

    public boolean isDoublePaidSaleSunday(LocalDate date) {
        if (date.getDayOfWeek() != DayOfWeek.SUNDAY || !isSaleMonth(date)) {
            return false;
        }
        LocalDate firstDayOfMonth = date.withDayOfMonth(1);
        LocalDate firstSunday = firstDayOfMonth.with(TemporalAdjusters.firstInMonth(DayOfWeek.SUNDAY));
        LocalDate secondSunday = firstSunday.plusWeeks(1);
        return date.equals(firstSunday) || date.equals(secondSunday);
    }

    private LocalDate normalizeToMonday(LocalDate date) {
        LocalDate safe = date == null ? LocalDate.now() : date;
        return safe.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private String weekId(LocalDate monday) {
        return monday.toString();
    }

    private int estimatedRequiredStaff(LocalDate date, int hour, int base) {
        int required = base;
        if (hour >= 11 && hour <= 13) {
            required += 1;
        }
        if (hour >= 17 && hour <= 19) {
            required += 2;
        }
        if (date.getDayOfWeek() == DayOfWeek.SATURDAY) {
            required += 1;
        }
        if (isSaleMonth(date) || isSummerMonth(date)) {
            required += 1;
        }
        if (isDoublePaidSaleSunday(date)) {
            required += 2;
        }
        return required;
    }

    private int plannedStaffAt(List<WorkShift> shifts, LocalDate date, int hour) {
        LocalTime slotStart = LocalTime.of(hour, 0);
        LocalTime slotEnd = slotStart.plusHours(1);
        return (int) shifts.stream()
            .filter(WorkShift::isActive)
            .filter(shift -> date.equals(shift.getShiftDate()))
            .filter(shift -> shift.getStartTime().isBefore(slotEnd) && shift.getEndTime().isAfter(slotStart))
            .count();
    }

    public Map<Integer, StaffingNeedSlot> aggregateNeedsByHour(List<StaffingNeedSlot> slots) {
        Map<Integer, List<StaffingNeedSlot>> byHour = slots.stream()
            .collect(Collectors.groupingBy(StaffingNeedSlot::getHour));
        Map<Integer, StaffingNeedSlot> result = new HashMap<>();
        for (Map.Entry<Integer, List<StaffingNeedSlot>> entry : byHour.entrySet()) {
            int required = entry.getValue().stream().mapToInt(StaffingNeedSlot::getRequiredStaff).max().orElse(0);
            int planned = (int) Math.round(entry.getValue().stream().mapToInt(StaffingNeedSlot::getPlannedStaff).average().orElse(0));
            result.put(entry.getKey(), new StaffingNeedSlot(null, entry.getKey(), required, planned));
        }
        return result;
    }

    public static final class ShiftValidation {
        private final boolean valid;
        private final String message;
        private final String breakPlan;
        private final double paidHours;

        private ShiftValidation(boolean valid, String message, String breakPlan, double paidHours) {
            this.valid = valid;
            this.message = message;
            this.breakPlan = breakPlan;
            this.paidHours = BigDecimal.valueOf(paidHours).setScale(2, RoundingMode.HALF_UP).doubleValue();
        }

        private static ShiftValidation valid(String message, String breakPlan, double paidHours) {
            return new ShiftValidation(true, message, breakPlan, paidHours);
        }

        private static ShiftValidation invalid(String message) {
            return new ShiftValidation(false, message, "", 0);
        }

        private static ShiftValidation rest(String message) {
            return new ShiftValidation(true, message, "", 0);
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }

        public String getBreakPlan() {
            return breakPlan;
        }

        public double getPaidHours() {
            return paidHours;
        }
    }

    public static final class StaffingNeedSlot {
        private final LocalDate date;
        private final int hour;
        private final int requiredStaff;
        private final int plannedStaff;

        public StaffingNeedSlot(LocalDate date, int hour, int requiredStaff, int plannedStaff) {
            this.date = date;
            this.hour = hour;
            this.requiredStaff = requiredStaff;
            this.plannedStaff = plannedStaff;
        }

        public LocalDate getDate() {
            return date;
        }

        public int getHour() {
            return hour;
        }

        public int getRequiredStaff() {
            return requiredStaff;
        }

        public int getPlannedStaff() {
            return plannedStaff;
        }
    }
}
