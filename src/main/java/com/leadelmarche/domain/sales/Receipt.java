package com.leadelmarche.domain.sales;

import com.leadelmarche.common.BaseEntity;
import java.time.LocalDateTime;

public class Receipt extends BaseEntity {
    private String saleId;
    private String textBody;
    private boolean printed;
    private boolean emailed;
    private String customerEmail;

    public Receipt() {
        super();
        this.textBody = "";
    }

    public Receipt(String id, boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(id, active, createdAt, updatedAt);
        this.textBody = "";
    }

    public String getSaleId() {
        return saleId;
    }

    public void setSaleId(String saleId) {
        this.saleId = saleId;
        touch();
    }

    public String getTextBody() {
        return textBody;
    }

    public void setTextBody(String textBody) {
        this.textBody = textBody;
        touch();
    }

    public boolean isPrinted() {
        return printed;
    }

    public void setPrinted(boolean printed) {
        this.printed = printed;
        touch();
    }

    public boolean isEmailed() {
        return emailed;
    }

    public void setEmailed(boolean emailed) {
        this.emailed = emailed;
        touch();
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
        touch();
    }
}

