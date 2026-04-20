package com.leadelmarche.domain.promotion;

import com.leadelmarche.common.BaseEntity;
import com.leadelmarche.domain.sales.SaleLine;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public abstract class Promotion extends BaseEntity {
    private String name;
    private boolean renewable;
    private LocalDate startDate;
    private LocalDate endDate;
    private PromotionEffect effect;

    protected Promotion() {
        super();
        this.startDate = LocalDate.now();
        this.endDate = LocalDate.now().plusMonths(1);
        this.effect = PromotionEffect.IMMEDIATE_DISCOUNT;
    }

    protected Promotion(String id, boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(id, active, createdAt, updatedAt);
        this.startDate = LocalDate.now();
        this.endDate = LocalDate.now().plusMonths(1);
        this.effect = PromotionEffect.IMMEDIATE_DISCOUNT;
    }

    public boolean isActiveAt(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate) && isActive();
    }

    public abstract BigDecimal computeDiscount(List<SaleLine> lines);

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        touch();
    }

    public boolean isRenewable() {
        return renewable;
    }

    public void setRenewable(boolean renewable) {
        this.renewable = renewable;
        touch();
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
        touch();
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
        touch();
    }

    public PromotionEffect getEffect() {
        return effect;
    }

    public void setEffect(PromotionEffect effect) {
        this.effect = effect;
        touch();
    }
}

