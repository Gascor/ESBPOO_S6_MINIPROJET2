package com.leadelmarche.service;

import com.leadelmarche.domain.customer.Customer;
import com.leadelmarche.domain.inventory.Product;
import com.leadelmarche.domain.inventory.ProductType;
import com.leadelmarche.domain.sales.PaymentMode;
import com.leadelmarche.domain.sales.Receipt;
import com.leadelmarche.domain.sales.Sale;
import com.leadelmarche.domain.sales.SaleLine;
import com.leadelmarche.domain.sales.SaleStatus;
import com.leadelmarche.persistence.SaleLineRepository;
import com.leadelmarche.persistence.SaleRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SalesService {
    private final SaleRepository saleRepository;
    private final SaleLineRepository saleLineRepository;
    private final InventoryService inventoryService;
    private final CustomerService customerService;
    private final PromotionService promotionService;
    private final ReceiptService receiptService;
    private final FastCheckoutService fastCheckoutService;

    private final Map<String, List<SaleLine>> draftLines = new ConcurrentHashMap<>();

    public SalesService(
        SaleRepository saleRepository,
        SaleLineRepository saleLineRepository,
        InventoryService inventoryService,
        CustomerService customerService,
        PromotionService promotionService,
        ReceiptService receiptService,
        FastCheckoutService fastCheckoutService
    ) {
        this.saleRepository = saleRepository;
        this.saleLineRepository = saleLineRepository;
        this.inventoryService = inventoryService;
        this.customerService = customerService;
        this.promotionService = promotionService;
        this.receiptService = receiptService;
        this.fastCheckoutService = fastCheckoutService;
    }

    public Sale startSale(String posId, String cashierBadge, String customerCard) {
        Customer customer = customerService.findOrAnonymous(customerCard);
        Sale sale = new Sale();
        sale.setSaleNumber(generateSaleNumber());
        sale.setPosId(posId);
        sale.setStoreId(InventoryService.DEFAULT_STORE_ID);
        sale.setCashierBadge(cashierBadge);
        sale.setCustomerId(customer.getId());
        sale.setSoldAt(LocalDateTime.now());
        sale.setStatus(SaleStatus.DRAFT);
        saleRepository.create(sale);
        draftLines.put(sale.getId(), new ArrayList<>());
        return sale;
    }

    public SaleLine addLineByBarcode(String saleId, String barcode, BigDecimal qty) {
        if (qty == null || qty.signum() <= 0) {
            throw new IllegalArgumentException("Quantity must be > 0");
        }
        Sale sale = requireDraftSale(saleId);
        Product product = inventoryService.findByBarcode(barcode)
            .orElseThrow(() -> new IllegalStateException("Unknown barcode: " + barcode));
        SaleLine line = addOrMergeLine(saleId, product, qty, BigDecimal.ZERO);
        recalculateAndPersistDraft(sale, draftLines.get(saleId));
        return line;
    }

    public SaleLine addLineByBarcodeWithMeasuredWeight(String saleId, String barcode, BigDecimal qty, BigDecimal measuredKg) {
        if (qty == null || qty.signum() <= 0) {
            throw new IllegalArgumentException("Quantity must be > 0");
        }
        Sale sale = requireDraftSale(saleId);
        Product product = inventoryService.findByBarcode(barcode)
            .orElseThrow(() -> new IllegalStateException("Unknown barcode: " + barcode));
        if (!fastCheckoutService.validateScannedWeight(product, measuredKg)) {
            throw new IllegalStateException(fastCheckoutService.requestCashierHelp(sale.getPosId(), "Weight mismatch"));
        }
        SaleLine line = addOrMergeLine(saleId, product, qty, BigDecimal.ZERO);
        recalculateAndPersistDraft(sale, draftLines.get(saleId));
        return line;
    }

    public SaleLine addWeightedLine(String saleId, ProductType productType, BigDecimal weightKg) {
        if (weightKg == null || weightKg.signum() <= 0) {
            throw new IllegalArgumentException("Weight must be > 0");
        }
        Sale sale = requireDraftSale(saleId);
        Product product = inventoryService.findWeightedProductByType(productType)
            .orElseThrow(() -> new IllegalStateException("No weighted product for type: " + productType));
        SaleLine line = addOrMergeLine(saleId, product, weightKg, weightKg);
        recalculateAndPersistDraft(sale, draftLines.get(saleId));
        return line;
    }

    public SaleLine addLineByProductId(String saleId, String productId, BigDecimal qty) {
        if (qty == null || qty.signum() <= 0) {
            throw new IllegalArgumentException("Quantity must be > 0");
        }
        Sale sale = requireDraftSale(saleId);
        Product product = inventoryService.findProductById(productId)
            .orElseThrow(() -> new IllegalStateException("Unknown product: " + productId));
        SaleLine line = addOrMergeLine(saleId, product, qty, BigDecimal.ZERO);
        recalculateAndPersistDraft(sale, draftLines.get(saleId));
        return line;
    }

    public SaleLine addWeightedLineByProductId(String saleId, String productId, BigDecimal weightKg) {
        if (weightKg == null || weightKg.signum() <= 0) {
            throw new IllegalArgumentException("Weight must be > 0");
        }
        Sale sale = requireDraftSale(saleId);
        Product product = inventoryService.findProductById(productId)
            .orElseThrow(() -> new IllegalStateException("Unknown product: " + productId));
        if (!product.isWeighted()) {
            throw new IllegalStateException("Selected product is not weighted: " + product.getName());
        }
        SaleLine line = addOrMergeLine(saleId, product, weightKg, weightKg);
        recalculateAndPersistDraft(sale, draftLines.get(saleId));
        return line;
    }

    public void reduceLineQuantity(String saleId, String lineId, BigDecimal qty) {
        if (qty == null || qty.signum() <= 0) {
            return;
        }
        Sale sale = requireDraftSale(saleId);
        List<SaleLine> lines = getDraftLinesInternal(saleId);
        for (int i = 0; i < lines.size(); i++) {
            SaleLine line = lines.get(i);
            if (!line.getId().equals(lineId)) {
                continue;
            }
            BigDecimal newQty = line.getQuantity().subtract(qty);
            if (newQty.signum() <= 0) {
                lines.remove(i);
            } else {
                line.setQuantity(newQty);
                line.recomputeTotals();
            }
            recalculateAndPersistDraft(sale, lines);
            return;
        }
        throw new IllegalStateException("Line not found: " + lineId);
    }

    public void removeLine(String saleId, String lineId) {
        Sale sale = requireDraftSale(saleId);
        List<SaleLine> lines = getDraftLinesInternal(saleId);
        lines.removeIf(line -> line.getId().equals(lineId));
        recalculateAndPersistDraft(sale, lines);
    }

    public void changePaymentMode(String saleId, PaymentMode mode) {
        Sale sale = requireDraftSale(saleId);
        sale.setPaymentMode(mode);
        saleRepository.update(sale);
    }

    public String requestCashierHelp(String saleId, String reason) {
        Sale sale = getSale(saleId);
        return fastCheckoutService.requestCashierHelp(sale.getPosId(), reason == null ? "Unknown issue" : reason);
    }

    public List<SaleLine> getDraftLines(String saleId) {
        return List.copyOf(getDraftLinesInternal(saleId));
    }

    public Sale getSale(String saleId) {
        return saleRepository.findById(saleId).orElseThrow(() -> new IllegalStateException("Sale not found: " + saleId));
    }

    public Receipt finalizeSale(String saleId) {
        Sale sale = requireDraftSale(saleId);
        List<SaleLine> lines = getDraftLinesInternal(saleId);
        if (lines.isEmpty()) {
            throw new IllegalStateException("Cannot finalize empty sale");
        }

        for (SaleLine line : lines) {
            BigDecimal stock = inventoryService.getAvailableStock(line.getProductId(), sale.getStoreId());
            if (stock.compareTo(line.getQuantity()) < 0) {
                throw new IllegalStateException("Insufficient stock for " + line.getProductName());
            }
        }

        Customer customer = customerService.getById(sale.getCustomerId())
            .orElseGet(() -> customerService.findOrAnonymous(null));

        sale.recalculateTotals(lines);
        PromotionComputation promotionComputation = promotionService.applyPromotions(sale, lines, customer);
        applyDiscountOnLines(lines, promotionComputation.getImmediateDiscount());
        sale.recalculateTotals(lines);
        sale.setDiscountTotal(promotionComputation.getImmediateDiscount());
        sale.setLoyaltyCredit(promotionComputation.getLoyaltyCredit());
        sale.setStatus(SaleStatus.FINALIZED);
        sale.setSoldAt(LocalDateTime.now());
        saleRepository.update(sale);

        for (SaleLine line : lines) {
            line.setSaleId(sale.getId());
            saleLineRepository.create(line);
            inventoryService.removeStock(line.getProductId(), sale.getStoreId(), line.getQuantity());
        }
        if (!customer.isAnonymous()) {
            customerService.creditLoyalty(customer.getLoyaltyCardNumber(), promotionComputation.getLoyaltyCredit());
        }

        Receipt receipt = receiptService.buildReceipt(sale, lines, customer, promotionComputation);
        draftLines.remove(saleId);
        return receipt;
    }

    public String printReceipt(String saleId) {
        Receipt receipt = receiptService.findBySaleId(saleId);
        return receiptService.print(receipt);
    }

    public String emailReceipt(String saleId, String email) {
        Receipt receipt = receiptService.findBySaleId(saleId);
        String recipient = email == null || email.isBlank() ? receipt.getCustomerEmail() : email.trim();
        if (recipient == null || recipient.isBlank()) {
            throw new IllegalArgumentException("Email destinataire obligatoire pour envoyer le ticket");
        }
        return receiptService.email(receipt, recipient);
    }

    private Sale requireDraftSale(String saleId) {
        Sale sale = getSale(saleId);
        if (sale.getStatus() != SaleStatus.DRAFT) {
            throw new IllegalStateException("Sale is not editable");
        }
        return sale;
    }

    private SaleLine addOrMergeLine(String saleId, Product product, BigDecimal qty, BigDecimal weightKg) {
        List<SaleLine> lines = getDraftLinesInternal(saleId);
        for (SaleLine line : lines) {
            if (line.getProductId().equals(product.getId())) {
                line.setQuantity(line.getQuantity().add(qty));
                if (weightKg.signum() > 0) {
                    line.setWeightKg(line.getWeightKg().add(weightKg));
                }
                line.recomputeTotals();
                return line;
            }
        }
        SaleLine line = new SaleLine();
        line.setId(UUID.randomUUID().toString());
        line.setSaleId(saleId);
        line.setProductId(product.getId());
        line.setProductName(product.getName());
        line.setUnitPriceHT(product.getUnitPriceHT());
        line.setQuantity(qty);
        line.setWeightKg(weightKg);
        line.setVatPercent(product.getVatPercent());
        line.recomputeTotals();
        lines.add(line);
        return line;
    }

    private void recalculateAndPersistDraft(Sale sale, List<SaleLine> lines) {
        sale.recalculateTotals(lines);
        saleRepository.update(sale);
    }

    private List<SaleLine> getDraftLinesInternal(String saleId) {
        return draftLines.computeIfAbsent(saleId, ignored -> new ArrayList<>());
    }

    private void applyDiscountOnLines(List<SaleLine> lines, BigDecimal totalDiscount) {
        if (totalDiscount == null || totalDiscount.signum() <= 0) {
            lines.forEach(l -> l.setDiscountAmount(BigDecimal.ZERO));
            return;
        }
        // Repartition proportionnelle de la remise sur les lignes HT.
        BigDecimal baseSum = lines.stream()
            .map(l -> l.getUnitPriceHT().multiply(l.getQuantity()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (baseSum.signum() <= 0) {
            return;
        }
        BigDecimal distributed = BigDecimal.ZERO;
        for (int i = 0; i < lines.size(); i++) {
            SaleLine line = lines.get(i);
            BigDecimal discount;
            if (i == lines.size() - 1) {
                // Derniere ligne = rattrapage du reliquat d'arrondi pour conserver le total exact.
                discount = totalDiscount.subtract(distributed);
            } else {
                BigDecimal ratio = line.getUnitPriceHT().multiply(line.getQuantity()).divide(baseSum, 8, RoundingMode.HALF_UP);
                discount = totalDiscount.multiply(ratio).setScale(2, RoundingMode.HALF_UP);
                distributed = distributed.add(discount);
            }
            line.setDiscountAmount(discount.max(BigDecimal.ZERO));
            line.recomputeTotals();
        }
    }

    private String generateSaleNumber() {
        String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String shortId = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "V-" + stamp + "-" + shortId;
    }
}
