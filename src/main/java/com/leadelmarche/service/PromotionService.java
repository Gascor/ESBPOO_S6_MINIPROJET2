package com.leadelmarche.service;

import com.leadelmarche.domain.customer.Customer;
import com.leadelmarche.domain.sales.Sale;
import com.leadelmarche.domain.sales.SaleLine;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PromotionService {
    public PromotionComputation applyPromotions(Sale sale, List<SaleLine> lines, Customer customer) {
        PromotionComputation result = new PromotionComputation();
        BigDecimal immediateDiscount = computeImmediateDiscount(lines);
        result.setImmediateDiscount(immediateDiscount);
        if (immediateDiscount.signum() > 0) {
            result.getAppliedPromotions().add("Promo immediate appliquee");
        }

        if (customer != null && !customer.isAnonymous()) {
            BigDecimal subtotal = sale.getSubTotalHT();
            BigDecimal loyalty = subtotal.subtract(immediateDiscount)
                .max(BigDecimal.ZERO)
                .multiply(BigDecimal.valueOf(0.02))
                .setScale(2, RoundingMode.HALF_UP);
            result.setLoyaltyCredit(loyalty);
            if (loyalty.signum() > 0) {
                result.getAppliedPromotions().add("2% credite sur la cagnotte fidelite");
            }
        }
        return result;
    }

    private BigDecimal computeImmediateDiscount(List<SaleLine> lines) {
        BigDecimal discount = BigDecimal.ZERO;
        Map<String, BigDecimal> byProductQuantity = new HashMap<>();
        Map<String, BigDecimal> byProductPrice = new HashMap<>();

        for (SaleLine line : lines) {
            byProductQuantity.merge(line.getProductId(), line.getQuantity(), BigDecimal::add);
            byProductPrice.putIfAbsent(line.getProductId(), line.getUnitPriceHT());
        }

        for (Map.Entry<String, BigDecimal> entry : byProductQuantity.entrySet()) {
            BigDecimal qty = entry.getValue();
            BigDecimal price = byProductPrice.get(entry.getKey());
            // Rule 1: buy 2, 3rd free
            if (qty.compareTo(BigDecimal.valueOf(3)) >= 0) {
                int freeItems = qty.intValue() / 3;
                discount = discount.add(price.multiply(BigDecimal.valueOf(freeItems)));
                continue;
            }
            // Rule 2: 30% on 2nd identical product
            if (qty.compareTo(BigDecimal.valueOf(2)) >= 0) {
                discount = discount.add(price.multiply(BigDecimal.valueOf(0.30)));
            }
        }
        return discount.setScale(2, RoundingMode.HALF_UP);
    }
}

