package com.leadelmarche.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PromotionComputation {
    private BigDecimal immediateDiscount = BigDecimal.ZERO;
    private BigDecimal loyaltyCredit = BigDecimal.ZERO;
    private final List<String> appliedPromotions = new ArrayList<>();

    public BigDecimal getImmediateDiscount() {
        return immediateDiscount;
    }

    public void setImmediateDiscount(BigDecimal immediateDiscount) {
        this.immediateDiscount = immediateDiscount;
    }

    public BigDecimal getLoyaltyCredit() {
        return loyaltyCredit;
    }

    public void setLoyaltyCredit(BigDecimal loyaltyCredit) {
        this.loyaltyCredit = loyaltyCredit;
    }

    public List<String> getAppliedPromotions() {
        return appliedPromotions;
    }
}

