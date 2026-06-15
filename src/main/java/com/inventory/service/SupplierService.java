package com.inventory.service;

import com.inventory.cache.CacheProvider;
import com.inventory.entity.Supplier;
import com.inventory.event.EntityType;
import com.inventory.event.EventAction;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.kafka.InventoryEventProducer;
import com.inventory.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Supplier fetch APIs are served from an in-process LRU cache, falling back
 * to the DAO on a cache miss or failure. Save/update APIs additionally write
 * the latest snapshot into the Redis distributed cache.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SupplierService {

    private static final String KEY_PREFIX = "supplier::";
    private static final String KEY_ALL = "supplier::all";

    private final SupplierRepository supplierRepository;
    private final InventoryEventProducer eventProducer;

    @Qualifier("lruCacheProvider")
    private final CacheProvider readCache;

    @Qualifier("redisCacheProvider")
    private final CacheProvider redisCache;

    @Transactional(readOnly = true)
    public List<Supplier> getAll(int offset, int limit) {

        String cacheKey = KEY_ALL + ":" + offset + ":" + limit;

        Object cached = readCache.get(cacheKey);

        if (cached instanceof List<?>) {

            System.out.println(
                    "LRU CACHE HIT -> Suppliers Page " +
                            offset + "," + limit);

            return (List<Supplier>) cached;
        }

        System.out.println(
                "DATABASE HIT -> Suppliers Page " +
                        offset + "," + limit);
        Pageable pageable = PageRequest.of(offset / limit, limit);

        List<Supplier> suppliers = supplierRepository.findAll(pageable)
                .getContent();

        readCache.put(cacheKey, suppliers);

        return suppliers;
    }

    @Transactional(readOnly = true)
    public Supplier getById(Long id) {

        Object cached = readCache.get(KEY_PREFIX + id);
        if (cached instanceof Supplier) {

            System.out.println(
                    "LRU CACHE HIT -> Supplier " + id);

            return (Supplier) cached;
        }

        System.out.println(
                "CACHE MISS -> Supplier " + id);

        System.out.println(
                "DATABASE HIT -> Supplier " + id);
        Supplier supplier = loadById(id);
        readCache.put(KEY_PREFIX + id, supplier);
        return supplier;
    }

    public void delete(Long id) {

        Supplier supplier = loadById(id);
        supplierRepository.delete(supplier);

        readCache.evict(KEY_PREFIX + id);
        readCache.evict(KEY_ALL);
        redisCache.evict(KEY_PREFIX + id);
        eventProducer.publish(
                EntityType.SUPPLIER,
                EventAction.DELETE,
                id,
                null);
    }

    private void cacheOnWrite(Supplier saved) {

        redisCache.put(
                KEY_PREFIX + saved.getId(),
                saved);

        System.out.println(
                "REDIS CACHE UPDATED -> " +
                        KEY_PREFIX + saved.getId());

        readCache.put(
                KEY_PREFIX + saved.getId(),
                saved);

        System.out.println(
                "LRU CACHE UPDATED -> " +
                        KEY_PREFIX + saved.getId());

        readCache.evict(KEY_ALL);
    }

    private Supplier loadById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Supplier not found"));
    }

    public Supplier save(Supplier request) {

        Supplier supplier;

        if (request.getId() != null &&
                supplierRepository.existsById(request.getId())) {

            supplier = supplierRepository.findById(request.getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Supplier not found"));

            supplier.setName(request.getName());
            supplier.setEmail(request.getEmail());
            supplier.setPhone(request.getPhone());

            supplier = supplierRepository.save(supplier);

            eventProducer.publish(
                    "SUPPLIER",
                    "UPDATE",
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
                    "SUPPLIER",
                    "CREATE",
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
