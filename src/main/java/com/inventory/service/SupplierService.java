package com.inventory.service;

import com.inventory.cache.CacheProvider;
import com.inventory.dto.SupplierRequest;
import com.inventory.entity.Supplier;
import com.inventory.event.EntityType;
import com.inventory.event.EventAction;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.kafka.InventoryEventProducer;
import com.inventory.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public Supplier create(SupplierRequest request) {

        Supplier supplier =
                Supplier.builder()
                        .name(request.getName())
                        .email(request.getEmail())
                        .phone(request.getPhone())
                        .build();

        Supplier saved = supplierRepository.save(supplier);

        cacheOnWrite(saved);
        eventProducer.publish(
                EntityType.SUPPLIER,
                EventAction.CREATE,
                saved.getId(),
                saved);

        return saved;
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Supplier> getAll() {

        Object cached = readCache.get(KEY_ALL);
        if (cached instanceof List) {
            return (List<Supplier>) cached;
        }

        List<Supplier> suppliers = supplierRepository.findAll();
        readCache.put(KEY_ALL, suppliers);
        return suppliers;
    }

    @Transactional(readOnly = true)
    public Supplier getById(Long id) {

        Object cached = readCache.get(KEY_PREFIX + id);
        if (cached instanceof Supplier) {
            return (Supplier) cached;
        }

        Supplier supplier = loadById(id);
        readCache.put(KEY_PREFIX + id, supplier);
        return supplier;
    }

    public Supplier update(Long id, SupplierRequest request) {

        Supplier supplier = loadById(id);

        supplier.setName(request.getName());
        supplier.setEmail(request.getEmail());
        supplier.setPhone(request.getPhone());

        Supplier saved = supplierRepository.save(supplier);

        cacheOnWrite(saved);
        eventProducer.publish(
                EntityType.SUPPLIER,
                EventAction.UPDATE,
                saved.getId(),
                saved);

        return saved;
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
        redisCache.put(KEY_PREFIX + saved.getId(), saved);
        readCache.put(KEY_PREFIX + saved.getId(), saved);
        readCache.evict(KEY_ALL);
    }

    private Supplier loadById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Supplier not found"));
    }
}
