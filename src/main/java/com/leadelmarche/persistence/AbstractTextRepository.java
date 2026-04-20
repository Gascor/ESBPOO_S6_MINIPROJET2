package com.leadelmarche.persistence;

import com.leadelmarche.common.BaseEntity;
import com.leadelmarche.common.SearchNormalizer;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public abstract class AbstractTextRepository<T extends BaseEntity> implements Repository<T> {
    private final TextFileDatabase database;
    private final String fileName;

    protected AbstractTextRepository(TextFileDatabase database, String fileName) {
        this.database = database;
        this.fileName = fileName;
    }

    protected abstract T fromFields(List<String> fields);
    protected abstract List<String> toFields(T entity);
    protected abstract String nameOf(T entity);

    @Override
    public synchronized T create(T entity) {
        if (entity.getId() == null || entity.getId().isBlank()) {
            entity.setId(UUID.randomUUID().toString());
        }
        List<T> all = loadAll();
        all.add(entity);
        saveAll(all);
        return entity;
    }

    @Override
    public synchronized T update(T entity) {
        if (entity.getId() == null || entity.getId().isBlank()) {
            return create(entity);
        }
        List<T> all = loadAll();
        boolean updated = false;
        for (int i = 0; i < all.size(); i++) {
            if (entity.getId().equals(all.get(i).getId())) {
                all.set(i, entity);
                updated = true;
                break;
            }
        }
        if (!updated) {
            all.add(entity);
        }
        saveAll(all);
        return entity;
    }

    @Override
    public synchronized Optional<T> findById(String id) {
        return loadAll().stream().filter(e -> e.getId().equals(id)).findFirst();
    }

    @Override
    public synchronized List<T> findAll(boolean activeOnly) {
        List<T> all = loadAll();
        if (!activeOnly) {
            return all;
        }
        return all.stream().filter(BaseEntity::isActive).toList();
    }

    @Override
    public synchronized List<T> searchByName(String partial) {
        return findAll(true).stream()
            .filter(item -> SearchNormalizer.containsNormalized(nameOf(item), partial))
            .toList();
    }

    protected List<T> loadAll() {
        List<String> lines = database.readLines(fileName);
        List<T> result = new ArrayList<>();
        for (String line : lines) {
            if (line == null || line.isBlank()) {
                continue;
            }
            try {
                List<String> fields = DelimitedText.decode(line);
                result.add(fromFields(fields));
            } catch (Exception ignored) {
                // ignore malformed rows to keep backward compatibility
            }
        }
        return result;
    }

    protected void saveAll(List<T> entries) {
        List<String> lines = entries.stream().map(this::toFields).map(DelimitedText::encode).toList();
        database.writeLines(fileName, lines);
    }

    protected List<String> withBaseFields(BaseEntity entity) {
        List<String> fields = new ArrayList<>();
        fields.add(entity.getId());
        fields.add(Boolean.toString(entity.isActive()));
        fields.add(entity.getCreatedAt().toString());
        fields.add(entity.getUpdatedAt().toString());
        return fields;
    }

    protected int readBaseFields(BaseEntity entity, List<String> fields) {
        entity.setId(field(fields, 0));
        entity.setActive(Boolean.parseBoolean(field(fields, 1)));
        entity.setCreatedAt(parseDateTime(field(fields, 2)));
        entity.setUpdatedAt(parseDateTime(field(fields, 3)));
        return 4;
    }

    protected String field(List<String> fields, int index) {
        if (index < 0 || index >= fields.size()) {
            return "";
        }
        return fields.get(index);
    }

    protected BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isBlank()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException ex) {
            return BigDecimal.ZERO;
        }
    }

    protected LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) {
            return LocalDateTime.now();
        }
        try {
            return LocalDateTime.parse(value);
        } catch (Exception ex) {
            return LocalDateTime.now();
        }
    }
}

