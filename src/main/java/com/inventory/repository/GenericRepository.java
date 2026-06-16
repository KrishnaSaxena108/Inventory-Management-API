package com.inventory.repository;

import java.util.List;
import java.util.Optional;

public interface GenericRepository<T> {

    T save(T entity);

    Optional<T> findById(
            Class<T> entityClass,
            Long id);

    List<T> findAll(
            Class<T> entityClass);

    List<T> findAll(
            Class<T> entityClass,
            int offset,
            int limit);

    Optional<T> findOne(
            Class<T> entityClass,
            String field,
            Object value);

    List<T> findByField(
            Class<T> entityClass,
            String field,
            Object value);

    List<T> findByTwoFields(
            Class<T> entityClass,
            String field1,
            Object value1,
            String field2,
            Object value2);

    boolean existsById(
            Class<T> entityClass,
            Long id);

    void delete(T entity);
}