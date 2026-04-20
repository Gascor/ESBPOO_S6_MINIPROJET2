package com.leadelmarche.domain.promotion;

import com.leadelmarche.common.BaseEntity;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PromotionRule extends BaseEntity {
    private String name;
    private String productId;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean renewable;
    private PromotionEffect effect;
    private PromotionRuleType type;
    private int buyQty;
    private int freeQty;
    private int nthItem;
    private BigDecimal percentDiscount;

    public PromotionRule() {
        super();
        this.name = "";
        this.productId = "";
        this.startDate = LocalDate.now();
        this.endDate = LocalDate.now().plusDays(30);
        this.renewable = false;
        this.effect = PromotionEffect.IMMEDIATE_DISCOUNT;
        this.type = PromotionRuleType.BUY_X_GET_Y;
        this.buyQty = 2;
        this.freeQty = 1;
        this.nthItem = 2;
        this.percentDiscount = BigDecimal.valueOf(30);
    }

    public PromotionRule(String id, boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(id, active, createdAt, updatedAt);
        this.name = "";
        this.productId = "";
        this.startDate = LocalDate.now();
        this.endDate = LocalDate.now().plusDays(30);
        this.renewable = false;
        this.effect = PromotionEffect.IMMEDIATE_DISCOUNT;
        this.type = PromotionRuleType.BUY_X_GET_Y;
        this.buyQty = 2;
        this.freeQty = 1;
        this.nthItem = 2;
        this.percentDiscount = BigDecimal.valueOf(30);
    }

    public boolean appliesAt(LocalDate date) {
        if (date == null) {
            return false;
        }
        return !date.isBefore(startDate) && !date.isAfter(endDate) && isActive();
    }

    public boolean appliesToProduct(String candidateProductId) {
        if (productId == null || productId.isBlank()) {
            return true;
        }
        return productId.equals(candidateProductId);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        touch();
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
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

    public boolean isRenewable() {
        return renewable;
    }

    public void setRenewable(boolean renewable) {
        this.renewable = renewable;
        touch();
    }

    public PromotionEffect getEffect() {
        return effect;
    }

    public void setEffect(PromotionEffect effect) {
        this.effect = effect;
        touch();
    }

    public PromotionRuleType getType() {
        return type;
    }

    public void setType(PromotionRuleType type) {
        this.type = type;
        touch();
    }

    public int getBuyQty() {
        return buyQty;
    }

    public void setBuyQty(int buyQty) {
        this.buyQty = buyQty;
        touch();
    }

    public int getFreeQty() {
        return freeQty;
    }

    public void setFreeQty(int freeQty) {
        this.freeQty = freeQty;
        touch();
    }

    public int getNthItem() {
        return nthItem;
    }

    public void setNthItem(int nthItem) {
        this.nthItem = nthItem;
        touch();
    }

    public BigDecimal getPercentDiscount() {
        return percentDiscount;
    }

    public void setPercentDiscount(BigDecimal percentDiscount) {
        this.percentDiscount = percentDiscount;
        touch();
    }
}
