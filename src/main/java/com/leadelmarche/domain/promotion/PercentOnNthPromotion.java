package com.leadelmarche.domain.promotion;

import com.leadelmarche.domain.sales.SaleLine;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class PercentOnNthPromotion extends Promotion {
    private int nthItem;
    private BigDecimal percentDiscount;

    public PercentOnNthPromotion() {
        this.nthItem = 2;
        this.percentDiscount = BigDecimal.valueOf(30);
    }

    @Override
    public BigDecimal computeDiscount(List<SaleLine> lines) {
        BigDecimal discount = BigDecimal.ZERO;
        for (SaleLine line : lines) {
            int qty = line.getQuantity().setScale(0, RoundingMode.DOWN).intValue();
            if (qty >= nthItem) {
                BigDecimal oneItemDiscount = line.getUnitPriceHT()
                    .multiply(percentDiscount)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                discount = discount.add(oneItemDiscount);
            }
        }
        return discount;
    }

    public int getNthItem() {
        return nthItem;
    }

    public void setNthItem(int nthItem) {
        this.nthItem = nthItem;
    }

    public BigDecimal getPercentDiscount() {
        return percentDiscount;
    }

    public void setPercentDiscount(BigDecimal percentDiscount) {
        this.percentDiscount = percentDiscount;
    }
}

