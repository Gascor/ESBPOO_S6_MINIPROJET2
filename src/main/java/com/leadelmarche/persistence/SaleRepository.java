package com.leadelmarche.persistence;

import com.leadelmarche.domain.sales.PaymentMode;
import com.leadelmarche.domain.sales.Sale;
import com.leadelmarche.domain.sales.SaleStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class SaleRepository extends AbstractTextRepository<Sale> {
    public SaleRepository(TextFileDatabase database) {
        super(database, "sales.txt");
    }

    @Override
    protected Sale fromFields(List<String> fields) {
        Sale sale = new Sale();
        int offset = readBaseFields(sale, fields);
        sale.setSaleNumber(field(fields, offset++));
        sale.setPosId(field(fields, offset++));
        sale.setStoreId(field(fields, offset++));
        sale.setCashierBadge(field(fields, offset++));
        sale.setCustomerId(field(fields, offset++));
        sale.setSoldAt(parseDateTime(field(fields, offset++)));
        sale.setSubTotalHT(parseBigDecimal(field(fields, offset++)));
        sale.setTotalVat(parseBigDecimal(field(fields, offset++)));
        sale.setTotalTTC(parseBigDecimal(field(fields, offset++)));
        sale.setDiscountTotal(parseBigDecimal(field(fields, offset++)));
        sale.setLoyaltyCredit(parseBigDecimal(field(fields, offset++)));
        sale.setPaymentMode(parsePaymentMode(field(fields, offset++)));
        sale.setStatus(parseStatus(field(fields, offset)));
        return sale;
    }

    @Override
    protected List<String> toFields(Sale entity) {
        List<String> fields = withBaseFields(entity);
        fields.add(entity.getSaleNumber());
        fields.add(entity.getPosId());
        fields.add(entity.getStoreId());
        fields.add(entity.getCashierBadge());
        fields.add(entity.getCustomerId());
        fields.add(entity.getSoldAt().toString());
        fields.add(entity.getSubTotalHT().toPlainString());
        fields.add(entity.getTotalVat().toPlainString());
        fields.add(entity.getTotalTTC().toPlainString());
        fields.add(entity.getDiscountTotal().toPlainString());
        fields.add(entity.getLoyaltyCredit().toPlainString());
        fields.add(entity.getPaymentMode().name());
        fields.add(entity.getStatus().name());
        return fields;
    }

    @Override
    protected String nameOf(Sale entity) {
        return entity.getSaleNumber() + " " + entity.getCashierBadge();
    }

    public Optional<Sale> findBySaleNumber(String saleNumber) {
        return findAll(false).stream()
            .filter(s -> s.getSaleNumber() != null && s.getSaleNumber().equalsIgnoreCase(saleNumber))
            .findFirst();
    }

    private PaymentMode parsePaymentMode(String text) {
        try {
            return PaymentMode.valueOf(text);
        } catch (Exception ex) {
            return PaymentMode.CARD;
        }
    }

    private SaleStatus parseStatus(String text) {
        try {
            return SaleStatus.valueOf(text);
        } catch (Exception ex) {
            return SaleStatus.DRAFT;
        }
    }
}

