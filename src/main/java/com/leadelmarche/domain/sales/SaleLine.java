package com.leadelmarche.domain.sales;

import com.leadelmarche.common.BaseEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SaleLine extends BaseEntity {
    private String saleId;
    private String productId;
    private String productName;
    private BigDecimal unitPriceHT;
    private BigDecimal quantity;
    private BigDecimal weightKg;
    private BigDecimal vatPercent;
    private BigDecimal lineVat;
    private BigDecimal lineTotal;
    private BigDecimal discountAmount;

    public SaleLine() {
        super();
        this.unitPriceHT = BigDecimal.ZERO;
        this.quantity = BigDecimal.ZERO;
        this.weightKg = BigDecimal.ZERO;
        this.vatPercent = BigDecimal.ZERO;
        this.lineVat = BigDecimal.ZERO;
        this.lineTotal = BigDecimal.ZERO;
        this.discountAmount = BigDecimal.ZERO;
    }

    public SaleLine(String id, boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(id, active, createdAt, updatedAt);
        this.unitPriceHT = BigDecimal.ZERO;
        this.quantity = BigDecimal.ZERO;
        this.weightKg = BigDecimal.ZERO;
        this.vatPercent = BigDecimal.ZERO;
        this.lineVat = BigDecimal.ZERO;
        this.lineTotal = BigDecimal.ZERO;
        this.discountAmount = BigDecimal.ZERO;
    }

    public void recomputeTotals() {
        BigDecimal base = unitPriceHT.multiply(quantity);
        this.lineVat = base.multiply(vatPercent).divide(BigDecimal.valueOf(100));
        this.lineTotal = base.add(lineVat).subtract(discountAmount);
        touch();
    }

    public String getSaleId() {
        return saleId;
    }

    public void setSaleId(String saleId) {
        this.saleId = saleId;
        touch();
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
        touch();
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
        touch();
    }

    public BigDecimal getUnitPriceHT() {
        return unitPriceHT;
    }

    public void setUnitPriceHT(BigDecimal unitPriceHT) {
        this.unitPriceHT = unitPriceHT;
        touch();
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
        touch();
    }

    public BigDecimal getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(BigDecimal weightKg) {
        this.weightKg = weightKg;
        touch();
    }

    public BigDecimal getVatPercent() {
        return vatPercent;
    }

    public void setVatPercent(BigDecimal vatPercent) {
        this.vatPercent = vatPercent;
        touch();
    }

    public BigDecimal getLineVat() {
        return lineVat;
    }

    public void setLineVat(BigDecimal lineVat) {
        this.lineVat = lineVat;
        touch();
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
        touch();
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
        touch();
    }
}

