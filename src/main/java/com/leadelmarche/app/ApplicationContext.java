package com.leadelmarche.app;

import com.leadelmarche.persistence.AbsenceRepository;
import com.leadelmarche.persistence.CustomerRepository;
import com.leadelmarche.persistence.EmployeeRepository;
import com.leadelmarche.persistence.InventoryRepository;
import com.leadelmarche.persistence.ProductRepository;
import com.leadelmarche.persistence.ReceiptRepository;
import com.leadelmarche.persistence.SaleLineRepository;
import com.leadelmarche.persistence.SaleRepository;
import com.leadelmarche.persistence.TextFileDatabase;
import com.leadelmarche.service.AbsenceService;
import com.leadelmarche.service.CustomerService;
import com.leadelmarche.service.FastCheckoutService;
import com.leadelmarche.service.InventoryService;
import com.leadelmarche.service.PromotionService;
import com.leadelmarche.service.ReceiptService;
import com.leadelmarche.service.SalesService;
import com.leadelmarche.service.SeedDataService;
import com.leadelmarche.service.StaffService;
import com.leadelmarche.service.StatisticsService;
import java.nio.file.Path;

public class ApplicationContext {
    private static final ApplicationContext INSTANCE = new ApplicationContext();

    private final TextFileDatabase database;
    private final InventoryService inventoryService;
    private final StaffService staffService;
    private final CustomerService customerService;
    private final AbsenceService absenceService;
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
        SaleRepository saleRepository = new SaleRepository(database);
        SaleLineRepository saleLineRepository = new SaleLineRepository(database);
        ReceiptRepository receiptRepository = new ReceiptRepository(database);

        this.inventoryService = new InventoryService(productRepository, inventoryRepository);
        this.staffService = new StaffService(employeeRepository);
        this.customerService = new CustomerService(customerRepository);
        this.absenceService = new AbsenceService(absenceRepository);
        this.promotionService = new PromotionService();
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

    public CustomerService customerService() {
        return customerService;
    }

    public SalesService salesService() {
        return salesService;
    }

    public StatisticsService statisticsService() {
        return statisticsService;
    }
}
