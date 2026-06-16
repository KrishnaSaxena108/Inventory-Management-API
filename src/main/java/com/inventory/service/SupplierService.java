package com.inventory.service;

import com.inventory.cache.CacheProvider;
import com.inventory.entity.Supplier;
import com.inventory.event.EntityType;
import com.inventory.event.EventAction;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.kafka.InventoryEventProducer;
import com.inventory.repository.GenericRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplierService {

        private static final String KEY_PREFIX = "supplier::";
        private static final String KEY_ALL = "supplier::all";

        private final GenericRepository<Supplier> supplierRepository;
        private final InventoryEventProducer eventProducer;

        @Qualifier("lruCacheProvider")
        private final CacheProvider readCache;

        @Qualifier("redisCacheProvider")
        private final CacheProvider redisCache;

        @Transactional(readOnly = true)
        public List<Supplier> getAll(int offset, int limit) {

                String cacheKey = KEY_ALL + ":" + offset + ":" + limit;

                // =========================
                // 1. LRU Cache
                // =========================
                Object lruValue = readCache.get(cacheKey);

                if (lruValue instanceof List<?>) {

                        System.out.println(
                                        "LRU CACHE HIT -> Suppliers Page " +
                                                        offset + "," + limit);

                        return (List<Supplier>) lruValue;
                }

                System.out.println(
                                "LRU CACHE MISS -> Suppliers Page " +
                                                offset + "," + limit);

                // =========================
                // 2. Redis Cache
                // =========================
                Object redisValue = redisCache.get(cacheKey);

                if (redisValue instanceof List<?>) {

                        System.out.println(
                                        "REDIS CACHE HIT -> Suppliers Page " +
                                                        offset + "," + limit);

                        readCache.put(cacheKey, redisValue);

                        System.out.println(
                                        "LRU CACHE UPDATED FROM REDIS -> Suppliers Page " +
                                                        offset + "," + limit);

                        return (List<Supplier>) redisValue;
                }

                System.out.println(
                                "REDIS CACHE MISS -> Suppliers Page " +
                                                offset + "," + limit);

                // =========================
                // 3. Database
                // =========================
                System.out.println(
                                "DATABASE HIT -> Suppliers Page " +
                                                offset + "," + limit);

                List<Supplier> suppliers = supplierRepository.findAll(
                                Supplier.class,
                                offset,
                                limit);

                redisCache.put(cacheKey, suppliers);

                System.out.println(
                                "REDIS CACHE UPDATED -> Suppliers Page " +
                                                offset + "," + limit);

                readCache.put(cacheKey, suppliers);

                System.out.println(
                                "LRU CACHE UPDATED -> Suppliers Page " +
                                                offset + "," + limit);

                return suppliers;
        }

        @Transactional(readOnly = true)
        public Supplier getById(Long id) {

                String key = KEY_PREFIX + id;

                // =========================
                // 1. LRU Cache
                // =========================
                Object lruValue = readCache.get(key);

                if (lruValue instanceof Supplier) {

                        System.out.println(
                                        "LRU CACHE HIT -> Supplier " + id);

                        return (Supplier) lruValue;
                }

                System.out.println(
                                "LRU CACHE MISS -> Supplier " + id);

                // =========================
                // 2. Redis Cache
                // =========================
                Object redisValue = redisCache.get(key);

                if (redisValue instanceof Supplier) {

                        System.out.println(
                                        "REDIS CACHE HIT -> Supplier " + id);

                        readCache.put(key, redisValue);

                        System.out.println(
                                        "LRU CACHE UPDATED FROM REDIS -> Supplier " + id);

                        return (Supplier) redisValue;
                }

                System.out.println(
                                "REDIS CACHE MISS -> Supplier " + id);

                // =========================
                // 3. Database
                // =========================
                System.out.println(
                                "DATABASE HIT -> Supplier " + id);

                Supplier supplier = loadById(id);

                redisCache.put(key, supplier);

                System.out.println(
                                "REDIS CACHE UPDATED -> Supplier " + id);

                readCache.put(key, supplier);

                System.out.println(
                                "LRU CACHE UPDATED -> Supplier " + id);

                return supplier;
        }

        public void delete(Long id) {

                Supplier supplier = loadById(id);

                supplierRepository.delete(supplier);

                String key = KEY_PREFIX + id;

                // Remove from both caches
                readCache.evict(key);
                redisCache.evict(key);

                // Remove paginated cache entries
                readCache.evict(KEY_ALL);
                redisCache.evict(KEY_ALL);

                eventProducer.publish(
                                EntityType.SUPPLIER,
                                EventAction.DELETE,
                                id,
                                null);

                System.out.println(
                                "SUPPLIER DELETED -> " + id);
        }

        private void cacheOnWrite(Supplier supplier) {

                String key = KEY_PREFIX + supplier.getId();

                // Update Redis first
                redisCache.put(key, supplier);

                System.out.println(
                                "REDIS CACHE UPDATED -> " + key);

                // Update LRU
                readCache.put(key, supplier);

                System.out.println(
                                "LRU CACHE UPDATED -> " + key);

                // Clear paginated cache
                readCache.evict(KEY_ALL);
                redisCache.evict(KEY_ALL);
        }

        private Supplier loadById(Long id) {

                return supplierRepository.findById(Supplier.class, id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Supplier not found"));
        }

        public Supplier save(Supplier request) {

                Supplier supplier;

                if (request.getId() != null &&
                                supplierRepository.existsById(Supplier.class, request.getId())) {

                        supplier = supplierRepository.findById(Supplier.class, request.getId())
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "Supplier not found"));

                        supplier.setName(request.getName());
                        supplier.setEmail(request.getEmail());
                        supplier.setPhone(request.getPhone());

                        supplier = supplierRepository.save(supplier);

                        eventProducer.publish(
                                        EntityType.SUPPLIER,
                                        EventAction.UPDATE,
                                        supplier.getId(),
                                        supplier);

                        System.out.println(
                                        "SUPPLIER UPDATED -> " +
                                                        supplier.getId());

                } else {

                        supplier = Supplier.builder()
                                        .name(request.getName())
                                        .email(request.getEmail())
                                        .phone(request.getPhone())
                                        .build();

                        supplier = supplierRepository.save(supplier);

                        eventProducer.publish(
                                        EntityType.SUPPLIER,
                                        EventAction.CREATE,
                                        supplier.getId(),
                                        supplier);

                        System.out.println(
                                        "SUPPLIER CREATED -> " +
                                                        supplier.getId());
                }

                cacheOnWrite(supplier);

                return supplier;
        }
}