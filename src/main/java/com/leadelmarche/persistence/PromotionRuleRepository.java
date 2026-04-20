package com.leadelmarche.persistence;

import com.leadelmarche.domain.promotion.PromotionEffect;
import com.leadelmarche.domain.promotion.PromotionRule;
import com.leadelmarche.domain.promotion.PromotionRuleType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class PromotionRuleRepository extends AbstractTextRepository<PromotionRule> {
    public PromotionRuleRepository(TextFileDatabase database) {
        super(database, "promotion_rules.txt");
    }

    @Override
    protected PromotionRule fromFields(List<String> fields) {
        PromotionRule rule = new PromotionRule();
        int offset = readBaseFields(rule, fields);
        rule.setName(field(fields, offset++));
        rule.setProductId(field(fields, offset++));
        rule.setStartDate(parseDate(field(fields, offset++)));
        rule.setEndDate(parseDate(field(fields, offset++)));
        rule.setRenewable(Boolean.parseBoolean(field(fields, offset++)));
        rule.setEffect(parseEffect(field(fields, offset++)));
        rule.setType(parseType(field(fields, offset++)));
        rule.setBuyQty(parseInt(field(fields, offset++), 2));
        rule.setFreeQty(parseInt(field(fields, offset++), 1));
        rule.setNthItem(parseInt(field(fields, offset++), 2));
        rule.setPercentDiscount(parseDecimal(field(fields, offset)));
        return rule;
    }

    @Override
    protected List<String> toFields(PromotionRule entity) {
        List<String> fields = withBaseFields(entity);
        fields.add(entity.getName());
        fields.add(entity.getProductId());
        fields.add(entity.getStartDate().toString());
        fields.add(entity.getEndDate().toString());
        fields.add(Boolean.toString(entity.isRenewable()));
        fields.add(entity.getEffect().name());
        fields.add(entity.getType().name());
        fields.add(Integer.toString(entity.getBuyQty()));
        fields.add(Integer.toString(entity.getFreeQty()));
        fields.add(Integer.toString(entity.getNthItem()));
        fields.add(entity.getPercentDiscount().toPlainString());
        return fields;
    }

    @Override
    protected String nameOf(PromotionRule entity) {
        return entity.getName();
    }

    private LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text);
        } catch (Exception ex) {
            return LocalDate.now();
        }
    }

    private PromotionEffect parseEffect(String text) {
        try {
            return PromotionEffect.valueOf(text);
        } catch (Exception ex) {
            return PromotionEffect.IMMEDIATE_DISCOUNT;
        }
    }

    private PromotionRuleType parseType(String text) {
        try {
            return PromotionRuleType.valueOf(text);
        } catch (Exception ex) {
            return PromotionRuleType.BUY_X_GET_Y;
        }
    }

    private int parseInt(String text, int fallback) {
        try {
            return Integer.parseInt(text);
        } catch (Exception ex) {
            return fallback;
        }
    }

    private BigDecimal parseDecimal(String text) {
        if (text == null || text.isBlank()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(text);
        } catch (Exception ex) {
            return BigDecimal.ZERO;
        }
    }
}
