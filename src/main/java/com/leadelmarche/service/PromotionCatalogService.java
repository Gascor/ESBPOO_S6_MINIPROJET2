package com.leadelmarche.service;

import com.leadelmarche.domain.promotion.PromotionEffect;
import com.leadelmarche.domain.promotion.PromotionRule;
import com.leadelmarche.domain.promotion.PromotionRuleType;
import com.leadelmarche.persistence.PromotionRuleRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class PromotionCatalogService {
    private final PromotionRuleRepository repository;

    public PromotionCatalogService(PromotionRuleRepository repository) {
        this.repository = repository;
    }

    public PromotionRule createPromotion(
        String name,
        String productId,
        LocalDate startDate,
        LocalDate endDate,
        boolean renewable,
        PromotionEffect effect,
        PromotionRuleType type,
        int buyQty,
        int freeQty,
        int nthItem,
        BigDecimal percentDiscount
    ) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Nom promotion obligatoire");
        }
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Periode promotion invalide");
        }
        PromotionRule rule = new PromotionRule();
        rule.setName(name.trim());
        rule.setProductId(productId == null ? "" : productId.trim());
        rule.setStartDate(startDate);
        rule.setEndDate(endDate);
        rule.setRenewable(renewable);
        rule.setEffect(effect == null ? PromotionEffect.IMMEDIATE_DISCOUNT : effect);
        rule.setType(type == null ? PromotionRuleType.BUY_X_GET_Y : type);
        rule.setBuyQty(Math.max(1, buyQty));
        rule.setFreeQty(Math.max(1, freeQty));
        rule.setNthItem(Math.max(2, nthItem));
        rule.setPercentDiscount(
            percentDiscount == null
                ? BigDecimal.valueOf(30)
                : percentDiscount.max(BigDecimal.ZERO).min(BigDecimal.valueOf(100))
        );
        return repository.create(rule);
    }

    public List<PromotionRule> listPromotions(boolean activeOnly) {
        return repository.findAll(activeOnly);
    }

    public List<PromotionRule> listActivePromotionsAt(LocalDate date) {
        LocalDate target = date == null ? LocalDate.now() : date;
        return repository.findAll(true).stream()
            .filter(rule -> rule.appliesAt(target))
            .toList();
    }

    public void deactivatePromotion(String promotionId) {
        repository.findById(promotionId).ifPresent(rule -> {
            rule.setActive(false);
            repository.update(rule);
        });
    }
}
