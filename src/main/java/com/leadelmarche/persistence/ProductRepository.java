package com.leadelmarche.persistence;

import com.leadelmarche.domain.inventory.Product;
import com.leadelmarche.domain.inventory.ProductType;
import java.util.List;
import java.util.Optional;

public class ProductRepository extends AbstractTextRepository<Product> {
    public ProductRepository(TextFileDatabase database) {
        super(database, "products.txt");
    }

    @Override
    protected Product fromFields(List<String> fields) {
        Product p = new Product();
        int offset = readBaseFields(p, fields);
        p.setSku(field(fields, offset++));
        p.setBarcode(field(fields, offset++));
        p.setName(field(fields, offset++));
        p.setDescription(field(fields, offset++));
        p.setType(parseType(field(fields, offset++)));
        p.setUnitPriceHT(parseBigDecimal(field(fields, offset++)));
        p.setVatPercent(parseBigDecimal(field(fields, offset++)));
        p.setCountryOfOrigin(field(fields, offset++));
        p.setSupplierName(field(fields, offset++));
        p.setWeighted(Boolean.parseBoolean(field(fields, offset++)));
        p.setSoldByPieceWithoutBarcode(Boolean.parseBoolean(field(fields, offset++)));
        p.setExpectedWeightKg(parseBigDecimal(field(fields, offset++)));
        p.setLowStockThresholdPercent(parseBigDecimal(field(fields, offset)));
        return p;
    }

    @Override
    protected List<String> toFields(Product entity) {
        List<String> fields = withBaseFields(entity);
        fields.add(entity.getSku());
        fields.add(entity.getBarcode());
        fields.add(entity.getName());
        fields.add(entity.getDescription());
        fields.add(entity.getType().name());
        fields.add(entity.getUnitPriceHT().toPlainString());
        fields.add(entity.getVatPercent().toPlainString());
        fields.add(entity.getCountryOfOrigin());
        fields.add(entity.getSupplierName());
        fields.add(Boolean.toString(entity.isWeighted()));
        fields.add(Boolean.toString(entity.isSoldByPieceWithoutBarcode()));
        fields.add(entity.getExpectedWeightKg().toPlainString());
        fields.add(entity.getLowStockThresholdPercent().toPlainString());
        return fields;
    }

    @Override
    protected String nameOf(Product entity) {
        return entity.getName() + " " + entity.getSku() + " " + entity.getBarcode();
    }

    public Optional<Product> findByBarcode(String barcode) {
        return findAll(true).stream()
            .filter(p -> p.getBarcode() != null && p.getBarcode().equalsIgnoreCase(barcode))
            .findFirst();
    }

    private ProductType parseType(String value) {
        try {
            return ProductType.valueOf(value);
        } catch (Exception ex) {
            return ProductType.AUTRE;
        }
    }
}

