package com.leadelmarche.service;

import com.leadelmarche.domain.inventory.Product;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class FastCheckoutService {
    public boolean validateScannedWeight(Product product, BigDecimal measuredKg) {
        if (product == null || measuredKg == null) {
            return false;
        }
        BigDecimal expected = product.getExpectedWeightKg();
        if (expected == null || expected.signum() <= 0) {
            return true;
        }
        BigDecimal tolerance = expected.multiply(BigDecimal.valueOf(0.10)).setScale(3, RoundingMode.HALF_UP);
        BigDecimal min = expected.subtract(tolerance);
        BigDecimal max = expected.add(tolerance);
        return measuredKg.compareTo(min) >= 0 && measuredKg.compareTo(max) <= 0;
    }

    public String requestCashierHelp(String posId, String reason) {
        return "HELP REQUEST [" + posId + "]: " + reason;
    }
}

