package com.leadelmarche.service;

import com.leadelmarche.domain.customer.Customer;
import com.leadelmarche.domain.promotion.PromotionEffect;
import com.leadelmarche.domain.promotion.PromotionRule;
import com.leadelmarche.domain.promotion.PromotionRuleType;
import com.leadelmarche.domain.sales.Sale;
import com.leadelmarche.domain.sales.SaleLine;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class PromotionService {
    private final PromotionCatalogService promotionCatalogService;

    public PromotionService(PromotionCatalogService promotionCatalogService) {
        this.promotionCatalogService = promotionCatalogService;
    }

    public PromotionComputation applyPromotions(Sale sale, List<SaleLine> lines, Customer customer) {
        PromotionComputation result = new PromotionComputation();
        BigDecimal immediateDiscount = BigDecimal.ZERO;
        BigDecimal loyaltyCredit = BigDecimal.ZERO;

        List<PromotionRule> activeRules = promotionCatalogService.listActivePromotionsAt(sale.getSoldAt().toLocalDate());
        for (PromotionRule rule : activeRules) {
            BigDecimal amount = computeRuleAmount(rule, lines);
            if (amount.signum() <= 0) {
                continue;
            }
            if (rule.getEffect() == PromotionEffect.LOYALTY_POT) {
                loyaltyCredit = loyaltyCredit.add(amount);
            } else {
                immediateDiscount = immediateDiscount.add(amount);
            }
            result.getAppliedPromotions().add(rule.getName() + " (" + rule.getEffect() + ")");
        }

        if (customer != null && !customer.isAnonymous()) {
            BigDecimal subtotal = sale.getSubTotalHT();
            BigDecimal loyaltyBonus = subtotal.subtract(immediateDiscount)
                .max(BigDecimal.ZERO)
                .multiply(BigDecimal.valueOf(0.02))
                .setScale(2, RoundingMode.HALF_UP);
            loyaltyCredit = loyaltyCredit.add(loyaltyBonus);
            if (loyaltyBonus.signum() > 0) {
                result.getAppliedPromotions().add("2% credite sur la cagnotte fidelite");
            }
        }
        result.setImmediateDiscount(immediateDiscount.setScale(2, RoundingMode.HALF_UP));
        result.setLoyaltyCredit(loyaltyCredit.setScale(2, RoundingMode.HALF_UP));
        return result;
    }

    private BigDecimal computeRuleAmount(PromotionRule rule, List<SaleLine> lines) {
        BigDecimal amount = BigDecimal.ZERO;
        for (SaleLine line : lines) {
            if (!rule.appliesToProduct(line.getProductId())) {
                continue;
            }
            BigDecimal qty = line.getQuantity();
            if (rule.getType() == PromotionRuleType.BUY_X_GET_Y) {
                int packSize = Math.max(1, rule.getBuyQty()) + Math.max(1, rule.getFreeQty());
                int freeItems = qty.intValue() / packSize * Math.max(1, rule.getFreeQty());
                if (freeItems > 0) {
                    amount = amount.add(line.getUnitPriceHT().multiply(BigDecimal.valueOf(freeItems)));
                }
            } else if (rule.getType() == PromotionRuleType.PERCENT_ON_NTH) {
                int nth = Math.max(2, rule.getNthItem());
                int eligible = qty.intValue() / nth;
                if (eligible > 0) {
                    BigDecimal one = line.getUnitPriceHT()
                        .multiply(rule.getPercentDiscount())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    amount = amount.add(one.multiply(BigDecimal.valueOf(eligible)));
                }
            }
        }
        return amount.max(BigDecimal.ZERO);
    }
}
