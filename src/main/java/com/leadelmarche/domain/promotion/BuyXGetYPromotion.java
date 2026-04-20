package com.leadelmarche.domain.promotion;

import com.leadelmarche.domain.sales.SaleLine;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class BuyXGetYPromotion extends Promotion {
    private int xBought;
    private int yFree;

    public BuyXGetYPromotion() {
        this.xBought = 2;
        this.yFree = 1;
    }

    @Override
    public BigDecimal computeDiscount(List<SaleLine> lines) {
        BigDecimal discount = BigDecimal.ZERO;
        for (SaleLine line : lines) {
            int qty = line.getQuantity().setScale(0, RoundingMode.DOWN).intValue();
            int packs = qty / (xBought + yFree);
            if (packs > 0) {
                discount = discount.add(line.getUnitPriceHT().multiply(BigDecimal.valueOf(packs * yFree)));
            }
        }
        return discount;
    }

    public int getxBought() {
        return xBought;
    }

    public void setxBought(int xBought) {
        this.xBought = xBought;
    }

    public int getyFree() {
        return yFree;
    }

    public void setyFree(int yFree) {
        this.yFree = yFree;
    }
}

