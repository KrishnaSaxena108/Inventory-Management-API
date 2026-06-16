package com.inventory.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class GenericRepositoryImpl<T> implements GenericRepository<T> {

    @PersistenceContext
    private EntityManager entityManager;

    private Session session() {
        return entityManager.unwrap(Session.class);
    }

    @Override
    public T save(T entity) {

        try {

            Method method = entity.getClass().getMethod("getId");

            Object id = method.invoke(entity);

            if (id == null) {

                session().persist(entity);
                return entity;

            } else {

                return (T) session().merge(entity);
            }

        } catch (Exception ex) {

            throw new RuntimeException(
                    "Unable to save entity",
                    ex);
        }
    }

    @Override
    public Optional<T> findById(
            Class<T> entityClass,
            Long id) {

        return Optional.ofNullable(
                session().find(entityClass, id));
    }

    @Override
    public List<T> findAll(
            Class<T> entityClass) {

        return session().createQuery(
                "from " + entityClass.getSimpleName(),
                entityClass)
                .list();
    }

    @Override
    public List<T> findAll(
            Class<T> entityClass,
            int offset,
            int limit) {

        return session().createQuery(
                "from " + entityClass.getSimpleName(),
                entityClass)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .list();
    }

    @Override
    public Optional<T> findOne(
            Class<T> entityClass,
            String field,
            Object value) {

        List<T> list = session().createQuery(
                "from "
                        + entityClass.getSimpleName()
                        + " where "
                        + field
                        + " = :value",
                entityClass)
                .setParameter("value", value)
                .setMaxResults(1)
                .list();

        return list.stream().findFirst();
    }

    @Override
    public boolean existsById(
            Class<T> entityClass,
            Long id) {

        return session().find(entityClass, id) != null;
    }

    @Override
    public void delete(T entity) {

        session().remove(
                session().contains(entity)
                        ? entity
                        : session().merge(entity));
    }

    @Override
    public List<T> findByField(
            Class<T> entityClass,
            String field,
            Object value) {

        return session().createQuery(
                "from "
                        + entityClass.getSimpleName()
                        + " where "
                        + field
                        + " = :value",
                entityClass)
                .setParameter("value", value)
                .list();
    }

    @Override
    public List<T> findByTwoFields(
            Class<T> entityClass,
            String field1,
            Object value1,
            String field2,
            Object value2) {

        return session().createQuery(
                "from "
                        + entityClass.getSimpleName()
                        + " where "
                        + field1
                        + " = :value1 and "
                        + field2
                        + " = :value2",
                entityClass)
                .setParameter("value1", value1)
                .setParameter("value2", value2)
                .list();
    }
}