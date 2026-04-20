package com.leadelmarche.domain.sales;

import com.leadelmarche.common.BaseEntity;
import java.time.LocalDateTime;

public class POS extends BaseEntity {
    private String posId;
    private String name;
    private String storeId;
    private boolean fastCheckoutEnabled;

    public POS() {
        super();
    }

    public POS(String id, boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(id, active, createdAt, updatedAt);
    }

    public String getPosId() {
        return posId;
    }

    public void setPosId(String posId) {
        this.posId = posId;
        touch();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        touch();
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
        touch();
    }

    public boolean isFastCheckoutEnabled() {
        return fastCheckoutEnabled;
    }

    public void setFastCheckoutEnabled(boolean fastCheckoutEnabled) {
        this.fastCheckoutEnabled = fastCheckoutEnabled;
        touch();
    }
}

