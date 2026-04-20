package com.leadelmarche.app;

import com.leadelmarche.persistence.AbsenceRepository;
import com.leadelmarche.persistence.CustomerRepository;
import com.leadelmarche.persistence.EmployeeRepository;
import com.leadelmarche.persistence.InventoryRepository;
import com.leadelmarche.persistence.ProductRepository;
import com.leadelmarche.persistence.PromotionRuleRepository;
import com.leadelmarche.persistence.ReceiptRepository;
import com.leadelmarche.persistence.SaleLineRepository;
import com.leadelmarche.persistence.SaleRepository;
import com.leadelmarche.persistence.TextFileDatabase;
import com.leadelmarche.persistence.WorkShiftRepository;
import com.leadelmarche.service.AbsenceService;
import com.leadelmarche.service.CustomerService;
import com.leadelmarche.service.FastCheckoutService;
import com.leadelmarche.service.InventoryService;
import com.leadelmarche.service.PromotionCatalogService;
import com.leadelmarche.service.PromotionService;
import com.leadelmarche.service.ReceiptService;
import com.leadelmarche.service.ScheduleService;
import com.leadelmarche.service.SalesService;
import com.leadelmarche.service.SeedDataService;
import com.leadelmarche.service.SimplePdfService;
import com.leadelmarche.service.StaffService;
import com.leadelmarche.service.StatisticsService;
import com.leadelmarche.domain.promotion.PromotionEffect;
import com.leadelmarche.domain.promotion.PromotionRuleType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.nio.file.Path;

public class ApplicationContext {
    private static final ApplicationContext INSTANCE = new ApplicationContext();

    private final TextFileDatabase database;
    private final InventoryService inventoryService;
    private final StaffService staffService;
    private final CustomerService customerService;
    private final AbsenceService absenceService;
    private final ScheduleService scheduleService;
    private final PromotionCatalogService promotionCatalogService;
    private final PromotionService promotionService;
    private final FastCheckoutService fastCheckoutService;
    private final ReceiptService receiptService;
    private final SalesService salesService;
    private final StatisticsService statisticsService;
    private final SeedDataService seedDataService;

    private ApplicationContext() {
        this.database = new TextFileDatabase(Path.of("data"));

        ProductRepository productRepository = new ProductRepository(database);
        InventoryRepository inventoryRepository = new InventoryRepository(database);
        EmployeeRepository employeeRepository = new EmployeeRepository(database);
        CustomerRepository customerRepository = new CustomerRepository(database);
        AbsenceRepository absenceRepository = new AbsenceRepository(database);
        WorkShiftRepository workShiftRepository = new WorkShiftRepository(database);
        PromotionRuleRepository promotionRuleRepository = new PromotionRuleRepository(database);
        SaleRepository saleRepository = new SaleRepository(database);
        SaleLineRepository saleLineRepository = new SaleLineRepository(database);
        ReceiptRepository receiptRepository = new ReceiptRepository(database);

        this.inventoryService = new InventoryService(productRepository, inventoryRepository);
        this.staffService = new StaffService(employeeRepository);
        this.customerService = new CustomerService(customerRepository);
        this.absenceService = new AbsenceService(absenceRepository);
        this.scheduleService = new ScheduleService(
            staffService,
            absenceService,
            workShiftRepository,
            new SimplePdfService(),
            database
        );
        this.promotionCatalogService = new PromotionCatalogService(promotionRuleRepository);
        this.promotionService = new PromotionService(promotionCatalogService);
        this.fastCheckoutService = new FastCheckoutService();
        this.receiptService = new ReceiptService(receiptRepository, database);
        this.salesService = new SalesService(
            saleRepository,
            saleLineRepository,
            inventoryService,
            customerService,
            promotionService,
            receiptService,
            fastCheckoutService
        );
        this.statisticsService = new StatisticsService(
            customerRepository,
            saleRepository,
            saleLineRepository,
            absenceService
        );
        this.seedDataService = new SeedDataService(inventoryService, customerService, staffService, absenceService);
        this.seedDataService.loadDefaultData();
        seedPromotions();
    }

    public static ApplicationContext getInstance() {
        return INSTANCE;
    }

    public InventoryService inventoryService() {
        return inventoryService;
    }

    public StaffService staffService() {
        return staffService;
    }

    public AbsenceService absenceService() {
        return absenceService;
    }

    public ScheduleService scheduleService() {
        return scheduleService;
    }

    public CustomerService customerService() {
        return customerService;
    }

    public PromotionCatalogService promotionCatalogService() {
        return promotionCatalogService;
    }

    public SalesService salesService() {
        return salesService;
    }

    public StatisticsService statisticsService() {
        return statisticsService;
    }

    private void seedPromotions() {
        if (!promotionCatalogService.listPromotions(false).isEmpty()) {
            return;
        }
        LocalDate today = LocalDate.now();
        promotionCatalogService.createPromotion(
            "2 achetes, 1 offert",
            "",
            today.minusDays(3),
            today.plusMonths(2),
            true,
            PromotionEffect.IMMEDIATE_DISCOUNT,
            PromotionRuleType.BUY_X_GET_Y,
            2,
            1,
            2,
            BigDecimal.valueOf(30)
        );
        promotionCatalogService.createPromotion(
            "30% sur le 2eme produit (cagnotte)",
            "",
            today.minusDays(3),
            today.plusMonths(2),
            true,
            PromotionEffect.LOYALTY_POT,
            PromotionRuleType.PERCENT_ON_NTH,
            2,
            1,
            2,
            BigDecimal.valueOf(30)
        );
    }
}
