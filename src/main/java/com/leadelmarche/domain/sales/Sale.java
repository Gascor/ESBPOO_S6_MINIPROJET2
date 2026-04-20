package com.leadelmarche.domain.sales;

import com.leadelmarche.common.BaseEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Sale extends BaseEntity {
    private String saleNumber;
    private String posId;
    private String storeId;
    private String cashierBadge;
    private String customerId;
    private LocalDateTime soldAt;
    private BigDecimal subTotalHT;
    private BigDecimal totalVat;
    private BigDecimal totalTTC;
    private BigDecimal discountTotal;
    private BigDecimal loyaltyCredit;
    private PaymentMode paymentMode;
    private SaleStatus status;

    public Sale() {
        super();
        this.soldAt = LocalDateTime.now();
        this.subTotalHT = BigDecimal.ZERO;
        this.totalVat = BigDecimal.ZERO;
        this.totalTTC = BigDecimal.ZERO;
        this.discountTotal = BigDecimal.ZERO;
        this.loyaltyCredit = BigDecimal.ZERO;
        this.paymentMode = PaymentMode.CARD;
        this.status = SaleStatus.DRAFT;
    }

    public Sale(String id, boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(id, active, createdAt, updatedAt);
        this.soldAt = LocalDateTime.now();
        this.subTotalHT = BigDecimal.ZERO;
        this.totalVat = BigDecimal.ZERO;
        this.totalTTC = BigDecimal.ZERO;
        this.discountTotal = BigDecimal.ZERO;
        this.loyaltyCredit = BigDecimal.ZERO;
        this.paymentMode = PaymentMode.CARD;
        this.status = SaleStatus.DRAFT;
    }

    public void recalculateTotals(List<SaleLine> lines) {
        BigDecimal newSubTotalHT = BigDecimal.ZERO;
        BigDecimal newTotalVat = BigDecimal.ZERO;
        BigDecimal newTotalTTC = BigDecimal.ZERO;
        for (SaleLine line : lines) {
            line.recomputeTotals();
            BigDecimal lineBaseHT = line.getUnitPriceHT().multiply(line.getQuantity());
            newSubTotalHT = newSubTotalHT.add(lineBaseHT);
            newTotalVat = newTotalVat.add(line.getLineVat());
            newTotalTTC = newTotalTTC.add(line.getLineTotal());
        }
        this.subTotalHT = newSubTotalHT;
        this.totalVat = newTotalVat;
        this.totalTTC = newTotalTTC.subtract(discountTotal);
        touch();
    }

    public String getSaleNumber() {
        return saleNumber;
    }

    public void setSaleNumber(String saleNumber) {
        this.saleNumber = saleNumber;
        touch();
    }

    public String getPosId() {
        return posId;
    }

    public void setPosId(String posId) {
        this.posId = posId;
        touch();
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
        touch();
    }

    public String getCashierBadge() {
        return cashierBadge;
    }

    public void setCashierBadge(String cashierBadge) {
        this.cashierBadge = cashierBadge;
        touch();
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
        touch();
    }

    public LocalDateTime getSoldAt() {
        return soldAt;
    }

    public void setSoldAt(LocalDateTime soldAt) {
        this.soldAt = soldAt;
        touch();
    }

    public BigDecimal getSubTotalHT() {
        return subTotalHT;
    }

    public void setSubTotalHT(BigDecimal subTotalHT) {
        this.subTotalHT = subTotalHT;
        touch();
    }

    public BigDecimal getTotalVat() {
        return totalVat;
    }

    public void setTotalVat(BigDecimal totalVat) {
        this.totalVat = totalVat;
        touch();
    }

    public BigDecimal getTotalTTC() {
        return totalTTC;
    }

    public void setTotalTTC(BigDecimal totalTTC) {
        this.totalTTC = totalTTC;
        touch();
    }

    public BigDecimal getDiscountTotal() {
        return discountTotal;
    }

    public void setDiscountTotal(BigDecimal discountTotal) {
        this.discountTotal = discountTotal;
        touch();
    }

    public BigDecimal getLoyaltyCredit() {
        return loyaltyCredit;
    }

    public void setLoyaltyCredit(BigDecimal loyaltyCredit) {
        this.loyaltyCredit = loyaltyCredit;
        touch();
    }

    public PaymentMode getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(PaymentMode paymentMode) {
        this.paymentMode = paymentMode;
        touch();
    }

    public SaleStatus getStatus() {
        return status;
    }

    public void setStatus(SaleStatus status) {
        this.status = status;
        touch();
    }
}

