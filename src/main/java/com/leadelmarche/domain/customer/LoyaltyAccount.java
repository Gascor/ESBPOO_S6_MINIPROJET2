package com.leadelmarche.domain.customer;

import com.leadelmarche.common.BaseEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LoyaltyAccount extends BaseEntity {
    private String cardNumber;
    private BigDecimal potBalance;

    public LoyaltyAccount() {
        super();
        this.potBalance = BigDecimal.ZERO;
    }

    public LoyaltyAccount(String id, boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(id, active, createdAt, updatedAt);
        this.potBalance = BigDecimal.ZERO;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
        touch();
    }

    public BigDecimal getPotBalance() {
        return potBalance;
    }

    public void setPotBalance(BigDecimal potBalance) {
        this.potBalance = potBalance;
        touch();
    }
}

