package com.leadelmarche.domain.inventory;

import com.leadelmarche.common.BaseEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Product extends BaseEntity {
    private String sku;
    private String barcode;
    private String name;
    private String description;
    private ProductType type;
    private BigDecimal unitPriceHT;
    private String countryOfOrigin;
    private BigDecimal vatPercent;
    private String supplierName;
    private boolean weighted;
    private boolean soldByPieceWithoutBarcode;
    private BigDecimal expectedWeightKg;
    private BigDecimal lowStockThresholdPercent;

    public Product() {
        super();
        this.type = ProductType.AUTRE;
        this.unitPriceHT = BigDecimal.ZERO;
        this.vatPercent = BigDecimal.ZERO;
        this.expectedWeightKg = BigDecimal.ZERO;
        this.lowStockThresholdPercent = BigDecimal.valueOf(20);
    }

    public Product(String id, boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(id, active, createdAt, updatedAt);
        this.type = ProductType.AUTRE;
        this.unitPriceHT = BigDecimal.ZERO;
        this.vatPercent = BigDecimal.ZERO;
        this.expectedWeightKg = BigDecimal.ZERO;
        this.lowStockThresholdPercent = BigDecimal.valueOf(20);
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
        touch();
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
        touch();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        touch();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        touch();
    }

    public ProductType getType() {
        return type;
    }

    public void setType(ProductType type) {
        this.type = type;
        touch();
    }

    public BigDecimal getUnitPriceHT() {
        return unitPriceHT;
    }

    public void setUnitPriceHT(BigDecimal unitPriceHT) {
        this.unitPriceHT = unitPriceHT;
        touch();
    }

    public String getCountryOfOrigin() {
        return countryOfOrigin;
    }

    public void setCountryOfOrigin(String countryOfOrigin) {
        this.countryOfOrigin = countryOfOrigin;
        touch();
    }

    public BigDecimal getVatPercent() {
        return vatPercent;
    }

    public void setVatPercent(BigDecimal vatPercent) {
        this.vatPercent = vatPercent;
        touch();
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
        touch();
    }

    public boolean isWeighted() {
        return weighted;
    }

    public void setWeighted(boolean weighted) {
        this.weighted = weighted;
        touch();
    }

    public boolean isSoldByPieceWithoutBarcode() {
        return soldByPieceWithoutBarcode;
    }

    public void setSoldByPieceWithoutBarcode(boolean soldByPieceWithoutBarcode) {
        this.soldByPieceWithoutBarcode = soldByPieceWithoutBarcode;
        touch();
    }

    public BigDecimal getExpectedWeightKg() {
        return expectedWeightKg;
    }

    public void setExpectedWeightKg(BigDecimal expectedWeightKg) {
        this.expectedWeightKg = expectedWeightKg;
        touch();
    }

    public BigDecimal getLowStockThresholdPercent() {
        return lowStockThresholdPercent;
    }

    public void setLowStockThresholdPercent(BigDecimal lowStockThresholdPercent) {
        this.lowStockThresholdPercent = lowStockThresholdPercent;
        touch();
    }
}

