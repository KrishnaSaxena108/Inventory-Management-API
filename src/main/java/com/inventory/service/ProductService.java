package com.inventory.service;

import com.inventory.cache.CacheProvider;
import com.inventory.entity.Product;
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

/**
 * Product fetch APIs are served from the Redis distributed cache, falling
 * back to the DAO on a cache miss or backend failure. Save/update APIs
 * write the latest snapshot back into Redis.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private static final String KEY_PREFIX = "product::";
    private static final String KEY_ALL = "product::all";

    private final GenericRepository<Product> productRepository;
    private final GenericRepository<Supplier> supplierRepository;
    private final InventoryEventProducer eventProducer;

    @Qualifier("redisCacheProvider")
    private final CacheProvider cache;
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Product> getAll() {

        Object cached = cache.get(KEY_ALL);
        if (cached instanceof List) {
            return (List<Product>) cached;
        }

        List<Product> products = productRepository.findAll(Product.class);
        cache.put(KEY_ALL, products);
        return products;
    }

    @Transactional(readOnly = true)
    public Product getById(Long id) {

        Object cached = cache.get(KEY_PREFIX + id);
        if (cached instanceof Product) {
            return (Product) cached;
        }

        Product product = loadById(id);
        cache.put(KEY_PREFIX + id, product);
        return product;
    }

    public void delete(Long id) {

        Product product = loadById(id);
        productRepository.delete(product);

        cache.evict(KEY_PREFIX + id);
        cache.evict(KEY_ALL);
        eventProducer.publish(
                EntityType.PRODUCT,
                EventAction.DELETE,
                id,
                null);
    }

    private Product loadById(Long id) {
        return productRepository.findById(Product.class, id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found"));
    }

    private Supplier findSupplier(Long supplierId) {
        return supplierRepository.findById(Supplier.class, supplierId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Supplier not found"));
    }

    public Product save(Product product) {

        Product saved;

        if (product.getId() != null &&
                productRepository.existsById(Product.class, product.getId())) {

            Product existing = productRepository.findById(Product.class, product.getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found"));

            existing.setName(product.getName());
            existing.setDescription(product.getDescription());
            existing.setReorderLevel(product.getReorderLevel());
            existing.setSupplier(product.getSupplier());

            saved = productRepository.save(existing);

            eventProducer.publish(
                    "PRODUCT",
                    "UPDATE",
                    saved.getId(),
                    saved);

        } else {

            saved = productRepository.save(product);

            eventProducer.publish(
                    "PRODUCT",
                    "CREATE",
                    saved.getId(),
                    saved);
        }

        return saved;
    }
}