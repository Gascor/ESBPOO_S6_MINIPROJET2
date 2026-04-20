package com.leadelmarche.service;

import com.leadelmarche.domain.people.ContractType;
import com.leadelmarche.domain.people.Employee;
import com.leadelmarche.domain.people.StaffAbsence;
import com.leadelmarche.domain.people.WorkShift;
import com.leadelmarche.persistence.TextFileDatabase;
import com.leadelmarche.persistence.WorkShiftRepository;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
            BigDecimal seasonalNeed = isSaleMonth(month)
                ? requiredWeeklyHours.add(BigDecimal.valueOf(targetPerDay * 8L * 2L))
                : requiredWeeklyHours;
            if (availableWeeklyHours.compareTo(seasonalNeed) < 0) {
                alerts.add(
                    "Alerte effectif " + month.getYear() + "-" + String.format("%02d", month.getMonthValue())
                        + ": capacite " + availableWeeklyHours + "h/semaine < besoin " + seasonalNeed
                        + "h/semaine. Prevoir du personnel interimaire."
                );
            }
        }
        if (alerts.isEmpty()) {
            alerts.add("Pas d'alerte effectif sur les " + months + " prochains mois.");
        }
        return alerts;
    }

    private List<WorkShift> buildEmployeeWeek(Employee employee, LocalDate monday, int employeeIndex, String weekId) {
        int weeklyHours = weeklyContractHours(employee);
        Set<LocalDate> absences = absenceService.listAbsences(true).stream()
            .filter(a -> employee.getBadgeNumber().equalsIgnoreCase(a.getBadgeNumber()))
            .map(StaffAbsence::getAbsenceDate)
            .collect(Collectors.toSet());

        List<LocalDate> openDates = new ArrayList<>();
        for (int day = 0; day <= 6; day++) {
            LocalDate date = monday.plusDays(day);
            if (date.getDayOfWeek() != DayOfWeek.SUNDAY) {
                openDates.add(date);
            } else if (isDoublePaidSaleSunday(date)) {
                openDates.add(date);
            }
        }
        List<LocalDate> availableDates = openDates.stream()
            .filter(date -> !absences.contains(date))
            .toList();
        if (availableDates.isEmpty()) {
            return List.of();
        }

        int remaining = weeklyHours;
        List<WorkShift> shifts = new ArrayList<>();
        for (int idx = 0; idx < availableDates.size() && remaining > 0; idx++) {
            LocalDate date = availableDates.get(idx);
            int daysLeft = availableDates.size() - idx;
            int plannedHours = Math.min(8, (int) Math.ceil((double) remaining / daysLeft));
            plannedHours = Math.max(4, plannedHours);
            plannedHours = Math.min(plannedHours, remaining);

            LocalTime start = LocalTime.of(8 + (employeeIndex % 3), 0);
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

    private boolean isDoublePaidSaleSunday(LocalDate date) {
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
}
