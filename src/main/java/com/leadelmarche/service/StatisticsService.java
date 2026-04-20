package com.leadelmarche.service;

import com.leadelmarche.common.SearchNormalizer;
import com.leadelmarche.domain.sales.Sale;
import com.leadelmarche.domain.sales.SaleLine;
import com.leadelmarche.domain.sales.SaleStatus;
import com.leadelmarche.persistence.CustomerRepository;
import com.leadelmarche.persistence.SaleLineRepository;
import com.leadelmarche.persistence.SaleRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class StatisticsService {
    private interface StatisticComputer {
        BigDecimal compute(LocalDate start, LocalDate end);
    }

    public static final class StatisticOption {
        private final String id;
        private final String label;
        private final String unit;

        public StatisticOption(String id, String label, String unit) {
            this.id = id;
            this.label = label;
            this.unit = unit;
        }

        public String getId() {
            return id;
        }

        public String getLabel() {
            return label;
        }

        public String getUnit() {
            return unit;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public static final class ComparisonResult {
        private final StatisticOption option;
        private final BigDecimal periodA;
        private final BigDecimal periodB;
        private final BigDecimal deltaAbsolute;
        private final BigDecimal deltaPercent;

        public ComparisonResult(
            StatisticOption option,
            BigDecimal periodA,
            BigDecimal periodB,
            BigDecimal deltaAbsolute,
            BigDecimal deltaPercent
        ) {
            this.option = option;
            this.periodA = periodA;
            this.periodB = periodB;
            this.deltaAbsolute = deltaAbsolute;
            this.deltaPercent = deltaPercent;
        }

        public StatisticOption getOption() {
            return option;
        }

        public BigDecimal getPeriodA() {
            return periodA;
        }

        public BigDecimal getPeriodB() {
            return periodB;
        }

        public BigDecimal getDeltaAbsolute() {
            return deltaAbsolute;
        }

        public BigDecimal getDeltaPercent() {
            return deltaPercent;
        }
    }

    private final CustomerRepository customerRepository;
    private final SaleRepository saleRepository;
    private final SaleLineRepository saleLineRepository;
    private final AbsenceService absenceService;
    private final Map<String, StatisticOption> optionsById = new LinkedHashMap<>();
    private final Map<String, StatisticComputer> computersById = new LinkedHashMap<>();

    public StatisticsService(
        CustomerRepository customerRepository,
        SaleRepository saleRepository,
        SaleLineRepository saleLineRepository,
        AbsenceService absenceService
    ) {
        this.customerRepository = customerRepository;
        this.saleRepository = saleRepository;
        this.saleLineRepository = saleLineRepository;
        this.absenceService = absenceService;
        registerDefaultStats();
    }

    public List<StatisticOption> listAvailableStats() {
        return new ArrayList<>(optionsById.values());
    }

    public BigDecimal compute(String statId, LocalDate start, LocalDate end) {
        validatePeriod(start, end);
        StatisticComputer computer = computersById.get(statId);
        if (computer == null) {
            throw new IllegalArgumentException("Statistique inconnue: " + statId);
        }
        return normalize(computer.compute(start, end));
    }

    public ComparisonResult compare(
        String statId,
        LocalDate startA,
        LocalDate endA,
        LocalDate startB,
        LocalDate endB
    ) {
        validatePeriod(startA, endA);
        validatePeriod(startB, endB);
        StatisticOption option = optionsById.get(statId);
        if (option == null) {
            throw new IllegalArgumentException("Statistique inconnue: " + statId);
        }
        BigDecimal valueA = compute(statId, startA, endA);
        BigDecimal valueB = compute(statId, startB, endB);
        BigDecimal delta = normalize(valueA.subtract(valueB));
        BigDecimal deltaPercent;
        if (valueB.signum() == 0) {
            deltaPercent = valueA.signum() == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(100);
        } else {
            deltaPercent = normalize(
                valueA.subtract(valueB)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(valueB, 3, RoundingMode.HALF_UP)
            );
        }
        return new ComparisonResult(option, valueA, valueB, delta, deltaPercent);
    }

    public StatisticOption registerProductSalesStatistic(String label, String productNamePartial) {
        if (productNamePartial == null || productNamePartial.isBlank()) {
            throw new IllegalArgumentException("Nom produit obligatoire pour creer une statistique");
        }
        String id = "CUSTOM_PRODUCT_" + UUID.randomUUID();
        String safeLabel = label == null || label.isBlank()
            ? "Ventes du produit: " + productNamePartial.trim()
            : label.trim();
        String searchToken = productNamePartial.trim();
        registerCoreStatistic(
            id,
            safeLabel,
            "produits",
            (start, end) -> computeSoldQuantityForProduct(start, end, searchToken)
        );
        return optionsById.get(id);
    }

    public void recordAbsence(String badgeNumber, LocalDate absenceDate, String type, String note) {
        absenceService.recordAbsence(badgeNumber, absenceDate, type, note);
    }

    private void registerDefaultStats() {
        registerCoreStatistic(
            "LOYALTY_CARDS_CREATED",
            "Nombre de cartes fidelite creees",
            "cartes",
            this::computeLoyaltyCardsCreated
        );
        registerCoreStatistic(
            "PROMO_PRODUCTS_SOLD",
            "Nombre de produits vendus sur une promotion",
            "produits",
            this::computePromoProductsSold
        );
        registerCoreStatistic(
            "STAFF_ABSENCES",
            "Nombre d'absences du personnel",
            "absences",
            this::computeStaffAbsences
        );
    }

    private void registerCoreStatistic(String id, String label, String unit, StatisticComputer computer) {
        optionsById.put(id, new StatisticOption(id, label, unit));
        computersById.put(id, computer);
    }

    private BigDecimal computeLoyaltyCardsCreated(LocalDate start, LocalDate end) {
        long count = customerRepository.findAll(false).stream()
            .filter(customer -> !customer.isAnonymous())
            .filter(customer -> inPeriod(customer.getCreatedAt(), start, end))
            .count();
        return BigDecimal.valueOf(count);
    }

    private BigDecimal computePromoProductsSold(LocalDate start, LocalDate end) {
        Set<String> promoSaleIds = saleRepository.findAll(false).stream()
            .filter(this::isFinalized)
            .filter(sale -> inPeriod(sale.getSoldAt(), start, end))
            .filter(this::isPromotionSale)
            .map(Sale::getId)
            .collect(Collectors.toSet());
        if (promoSaleIds.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return saleLineRepository.findAll(false).stream()
            .filter(line -> promoSaleIds.contains(line.getSaleId()))
            .map(SaleLine::getQuantity)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal computeStaffAbsences(LocalDate start, LocalDate end) {
        long count = absenceService.listAbsences(false).stream()
            .filter(absence -> inPeriod(absence.getAbsenceDate(), start, end))
            .count();
        return BigDecimal.valueOf(count);
    }

    private BigDecimal computeSoldQuantityForProduct(LocalDate start, LocalDate end, String productNamePartial) {
        Set<String> finalizedSaleIds = saleRepository.findAll(false).stream()
            .filter(this::isFinalized)
            .filter(sale -> inPeriod(sale.getSoldAt(), start, end))
            .map(Sale::getId)
            .collect(Collectors.toSet());
        if (finalizedSaleIds.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return saleLineRepository.findAll(false).stream()
            .filter(line -> finalizedSaleIds.contains(line.getSaleId()))
            .filter(line -> SearchNormalizer.containsNormalized(line.getProductName(), productNamePartial))
            .map(SaleLine::getQuantity)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean isPromotionSale(Sale sale) {
        BigDecimal discount = sale.getDiscountTotal() == null ? BigDecimal.ZERO : sale.getDiscountTotal();
        BigDecimal loyalty = sale.getLoyaltyCredit() == null ? BigDecimal.ZERO : sale.getLoyaltyCredit();
        return discount.signum() > 0 || loyalty.signum() > 0;
    }

    private boolean isFinalized(Sale sale) {
        return sale != null && sale.getStatus() == SaleStatus.FINALIZED;
    }

    private boolean inPeriod(LocalDateTime timestamp, LocalDate start, LocalDate end) {
        if (timestamp == null) {
            return false;
        }
        LocalDate date = timestamp.toLocalDate();
        return inPeriod(date, start, end);
    }

    private boolean inPeriod(LocalDate date, LocalDate start, LocalDate end) {
        if (date == null) {
            return false;
        }
        return !date.isBefore(start) && !date.isAfter(end);
    }

    private void validatePeriod(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Periode invalide");
        }
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("La date de debut doit etre <= date de fin");
        }
    }

    private BigDecimal normalize(BigDecimal value) {
        BigDecimal safe = value == null ? BigDecimal.ZERO : value;
        BigDecimal normalized = safe.setScale(3, RoundingMode.HALF_UP).stripTrailingZeros();
        if (normalized.scale() < 0) {
            return normalized.setScale(0);
        }
        return normalized;
    }
}
