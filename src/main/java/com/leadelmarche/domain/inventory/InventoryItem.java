package com.leadelmarche.domain.inventory;

import com.leadelmarche.common.BaseEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InventoryItem extends BaseEntity {
    private String productId;
    private String storeId;
    private BigDecimal quantity;
    private BigDecimal reservedQuantity;

    public InventoryItem() {
        super();
        this.quantity = BigDecimal.ZERO;
        this.reservedQuantity = BigDecimal.ZERO;
    }

    public InventoryItem(String id, boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(id, active, createdAt, updatedAt);
        this.quantity = BigDecimal.ZERO;
        this.reservedQuantity = BigDecimal.ZERO;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
        touch();
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
        touch();
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
        touch();
    }

    public BigDecimal getReservedQuantity() {
        return reservedQuantity;
    }

    public void setReservedQuantity(BigDecimal reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
        touch();
    }

    public BigDecimal availableQuantity() {
        return quantity.subtract(reservedQuantity);
    }
}

