package com.leadelmarche.service;

import com.leadelmarche.common.Address;
import com.leadelmarche.domain.customer.Customer;
import com.leadelmarche.domain.inventory.Product;
import com.leadelmarche.domain.inventory.ProductType;
import com.leadelmarche.domain.people.ContractType;
import com.leadelmarche.domain.people.Employee;
import com.leadelmarche.domain.people.WorkShift;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

public class SeedDataService {
    private final InventoryService inventoryService;
    private final CustomerService customerService;
    private final StaffService staffService;
    private final AbsenceService absenceService;
    private final ScheduleService scheduleService;

    public SeedDataService(
        InventoryService inventoryService,
        CustomerService customerService,
        StaffService staffService,
        AbsenceService absenceService,
        ScheduleService scheduleService
    ) {
        this.inventoryService = inventoryService;
        this.customerService = customerService;
        this.staffService = staffService;
        this.absenceService = absenceService;
        this.scheduleService = scheduleService;
    }

    public void loadDefaultData() {
        seedProducts();
        seedCustomers();
        seedEmployees();
        seedAbsences();
        seedSchedulesForNextFourMonths();
    }

    private void seedProducts() {
        if (inventoryService.listProducts(false).size() >= 10) {
            return;
        }
        recordProduct("P-ALIM-001", "300000000001", "Pates", ProductType.ALIMENTAIRE, "1.40", "5.5", false, false, "0");
        recordProduct("P-ALIM-002", "300000000002", "Riz", ProductType.ALIMENTAIRE, "2.10", "5.5", false, false, "0");
        recordProduct("P-ALIM-003", "300000000003", "Pommes", ProductType.ALIMENTAIRE, "2.90", "5.5", true, false, "1.0");
        recordProduct("P-ALIM-004", "", "Avocat", ProductType.ALIMENTAIRE, "1.10", "5.5", false, true, "0");
        recordProduct("P-MED-001", "300000000005", "Vitamines", ProductType.MEDECINE, "8.50", "20", false, false, "0");
        recordProduct("P-MED-002", "300000000006", "Sirop", ProductType.MEDECINE, "6.30", "20", false, false, "0");
        recordProduct("P-ELEC-001", "300000000007", "Casque", ProductType.ELECTRONIQUE, "24.90", "20", false, false, "0");
        recordProduct("P-ELEC-002", "300000000008", "Chargeur USB-C", ProductType.ELECTRONIQUE, "14.90", "20", false, false, "0");
        recordProduct("P-AUTRE-001", "300000000009", "Carnet", ProductType.AUTRE, "2.40", "20", false, false, "0");
        recordProduct("P-AUTRE-002", "300000000010", "Stylo", ProductType.AUTRE, "1.20", "20", false, false, "0");
    }

    private void recordProduct(
        String sku,
        String barcode,
        String name,
        ProductType type,
        String unitPrice,
        String vat,
        boolean weighted,
        boolean soldByPiece,
        String expectedWeight
    ) {
        Product p = new Product();
        p.setSku(sku);
        p.setBarcode(barcode);
        p.setName(name);
        p.setDescription(name + " - produit seed");
        p.setType(type);
        p.setUnitPriceHT(new BigDecimal(unitPrice));
        p.setVatPercent(new BigDecimal(vat));
        p.setCountryOfOrigin("FR");
        p.setSupplierName("Fournisseur Demo");
        p.setWeighted(weighted);
        p.setSoldByPieceWithoutBarcode(soldByPiece);
        p.setExpectedWeightKg(new BigDecimal(expectedWeight));
        p.setLowStockThresholdPercent(BigDecimal.valueOf(20));
        Product created = inventoryService.createProduct(p);
        inventoryService.addStock(created.getId(), InventoryService.DEFAULT_STORE_ID, BigDecimal.valueOf(120));
    }

    private void seedCustomers() {
        customerService.ensureAnonymousCustomerExists();
        List<Customer> all = customerService.listCustomers(false);
        if (all.size() >= 11) {
            return;
        }
        for (int i = 1; i <= 10; i++) {
            Customer c = new Customer();
            c.setFirstName("Client" + i);
            c.setLastName("Demo" + i);
            c.setLoyaltyCardNumber("CARD-" + String.format("%04d", i));
            c.setEmail("client" + i + "@example.com");
            c.setPostalCode("7800" + (i % 10));
            c.setAnonymous(false);
            customerService.createCustomer(c);
        }
    }

    private void seedEmployees() {
        List<Employee> existing = staffService.listEmployees(false);
        if (existing.size() >= 10 && existing.stream().anyMatch(e -> "0166394".equalsIgnoreCase(e.getBadgeNumber()))) {
            return;
        }
        for (int i = 1; i <= 10; i++) {
            Employee employee = new Employee();
            employee.setBadgeNumber("B" + String.format("%03d", i));
            employee.setFirstName("Prenom" + i);
            employee.setLastName("Nom" + i);
            employee.setRole(i <= 6 ? "Caissier" : "Gestionnaire");
            employee.setSupervisorBadge(i <= 6 ? "B010" : "");
            employee.setHomeAddress(new Address("Rue " + i, "Versailles", "7800" + (i % 10), "France"));
            employee.setWorkAddress(new Address("LeadelMarche Centre", "Versailles", "78000", "France"));
            if (i <= 4) {
                employee.setContractType(ContractType.CDI_35H);
                employee.setContractWeeklyHours(35);
            } else if (i <= 6) {
                employee.setContractType(ContractType.CDI_ETUDIANT);
                employee.setContractWeeklyHours(18);
            } else if (i <= 8) {
                employee.setContractType(ContractType.CDD);
                employee.setContractWeeklyHours(35);
            } else {
                employee.setContractType(ContractType.INTERIM);
                employee.setContractWeeklyHours(30);
            }
            staffService.createEmployee(employee);
        }

        // Keep a realistic default profile for the workspace owner.
        if (staffService.findByBadge("0166394").isEmpty()) {
            Employee lucas = new Employee();
            lucas.setBadgeNumber("0166394");
            lucas.setFirstName("Lucas");
            lucas.setLastName("Da Silva Ferreira");
            lucas.setRole("Conseiller Commercial");
            lucas.setSupervisorBadge("B010");
            lucas.setHomeAddress(new Address("12 Rue de la Gare", "Versailles", "78000", "France"));
            lucas.setWorkAddress(new Address("LeadelMarche Centre", "Versailles", "78000", "France"));
            lucas.setContractType(ContractType.CDI_ETUDIANT);
            lucas.setContractWeeklyHours(15);
            staffService.createEmployee(lucas);
        }
    }

    private void seedAbsences() {
        if (absenceService.listAbsences(false).size() >= 10) {
            return;
        }
        for (int i = 1; i <= 10; i++) {
            String badge = "B" + String.format("%03d", i);
            String type = switch (i % 3) {
                case 0 -> "CONGE";
                case 1 -> "RTT";
                default -> "MALADIE";
            };
            LocalDate date = LocalDate.now().minusDays(i * 3L);
            absenceService.recordAbsence(badge, date, type, "Seed " + type.toLowerCase());
        }
    }

    private void seedSchedulesForNextFourMonths() {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate limit = today.plusMonths(4);
        while (!monday.isAfter(limit)) {
            List<WorkShift> week = scheduleService.getWeekSchedule(monday);
            boolean hasActiveWeek = week.stream().anyMatch(WorkShift::isActive);
            if (!hasActiveWeek) {
                scheduleService.generateWeeklySchedule(monday, 3);
            }
            monday = monday.plusWeeks(1);
        }
    }
}
