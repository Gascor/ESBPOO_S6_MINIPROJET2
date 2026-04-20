package com.leadelmarche.persistence;

import java.util.List;
import java.util.Optional;

public interface Repository<T> {
    T create(T entity);
    T update(T entity);
    Optional<T> findById(String id);
    List<T> findAll(boolean activeOnly);
    List<T> searchByName(String partial);
}

