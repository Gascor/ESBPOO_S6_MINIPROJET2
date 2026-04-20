package com.leadelmarche.domain.inventory;

import com.leadelmarche.common.Address;
import com.leadelmarche.common.BaseEntity;
import java.time.LocalDateTime;

public class Store extends BaseEntity {
    private String storeId;
    private String name;
    private Address address;
    private String openingHours;

    public Store() {
        super();
        this.address = new Address();
        this.openingHours = "Mon-Sat 08:00-20:00";
    }

    public Store(String id, boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(id, active, createdAt, updatedAt);
        this.address = new Address();
        this.openingHours = "Mon-Sat 08:00-20:00";
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
        touch();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        touch();
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
        touch();
    }

    public String getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(String openingHours) {
        this.openingHours = openingHours;
        touch();
    }
}

