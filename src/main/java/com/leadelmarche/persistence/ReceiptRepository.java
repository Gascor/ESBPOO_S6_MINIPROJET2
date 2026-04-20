package com.leadelmarche.persistence;

import com.leadelmarche.domain.sales.Receipt;
import java.util.List;
import java.util.Optional;

public class ReceiptRepository extends AbstractTextRepository<Receipt> {
    public ReceiptRepository(TextFileDatabase database) {
        super(database, "receipts.txt");
    }

    @Override
    protected Receipt fromFields(List<String> fields) {
        Receipt receipt = new Receipt();
        int offset = readBaseFields(receipt, fields);
        receipt.setSaleId(field(fields, offset++));
        receipt.setTextBody(field(fields, offset++));
        receipt.setPrinted(Boolean.parseBoolean(field(fields, offset++)));
        receipt.setEmailed(Boolean.parseBoolean(field(fields, offset++)));
        receipt.setCustomerEmail(field(fields, offset));
        return receipt;
    }

    @Override
    protected List<String> toFields(Receipt entity) {
        List<String> fields = withBaseFields(entity);
        fields.add(entity.getSaleId());
        fields.add(entity.getTextBody());
        fields.add(Boolean.toString(entity.isPrinted()));
        fields.add(Boolean.toString(entity.isEmailed()));
        fields.add(entity.getCustomerEmail());
        return fields;
    }

    @Override
    protected String nameOf(Receipt entity) {
        return entity.getSaleId();
    }

    public Optional<Receipt> findBySaleId(String saleId) {
        return findAll(false).stream().filter(r -> saleId.equals(r.getSaleId())).findFirst();
    }
}

