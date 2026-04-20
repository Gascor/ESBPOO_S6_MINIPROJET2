package com.leadelmarche.persistence;

import com.leadelmarche.domain.sales.SaleLine;
import java.util.List;

public class SaleLineRepository extends AbstractTextRepository<SaleLine> {
    public SaleLineRepository(TextFileDatabase database) {
        super(database, "sale_lines.txt");
    }

    @Override
    protected SaleLine fromFields(List<String> fields) {
        SaleLine line = new SaleLine();
        int offset = readBaseFields(line, fields);
        line.setSaleId(field(fields, offset++));
        line.setProductId(field(fields, offset++));
        line.setProductName(field(fields, offset++));
        line.setUnitPriceHT(parseBigDecimal(field(fields, offset++)));
        line.setQuantity(parseBigDecimal(field(fields, offset++)));
        line.setWeightKg(parseBigDecimal(field(fields, offset++)));
        line.setVatPercent(parseBigDecimal(field(fields, offset++)));
        line.setLineVat(parseBigDecimal(field(fields, offset++)));
        line.setLineTotal(parseBigDecimal(field(fields, offset++)));
        line.setDiscountAmount(parseBigDecimal(field(fields, offset)));
        return line;
    }

    @Override
    protected List<String> toFields(SaleLine entity) {
        List<String> fields = withBaseFields(entity);
        fields.add(entity.getSaleId());
        fields.add(entity.getProductId());
        fields.add(entity.getProductName());
        fields.add(entity.getUnitPriceHT().toPlainString());
        fields.add(entity.getQuantity().toPlainString());
        fields.add(entity.getWeightKg().toPlainString());
        fields.add(entity.getVatPercent().toPlainString());
        fields.add(entity.getLineVat().toPlainString());
        fields.add(entity.getLineTotal().toPlainString());
        fields.add(entity.getDiscountAmount().toPlainString());
        return fields;
    }

    @Override
    protected String nameOf(SaleLine entity) {
        return entity.getProductName() + " " + entity.getSaleId();
    }

    public List<SaleLine> findBySaleId(String saleId) {
        return findAll(false).stream().filter(l -> saleId.equals(l.getSaleId())).toList();
    }
}

