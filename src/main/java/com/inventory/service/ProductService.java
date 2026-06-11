package com.inventory.service;

import com.inventory.cache.CacheProvider;
import com.inventory.dto.ProductRequest;
import com.inventory.entity.Product;
import com.inventory.entity.Supplier;
import com.inventory.event.EntityType;
import com.inventory.event.EventAction;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.kafka.InventoryEventProducer;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.SupplierRepository;
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

    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final InventoryEventProducer eventProducer;

    @Qualifier("redisCacheProvider")
    private final CacheProvider cache;

    public Product create(ProductRequest request) {

        Supplier supplier = findSupplier(request.getSupplierId());

        Product product =
                Product.builder()
                        .name(request.getName())
                        .description(request.getDescription())
                        .reorderLevel(request.getReorderLevel())
                        .supplier(supplier)
                        .build();

        Product saved = productRepository.save(product);

        cache.put(KEY_PREFIX + saved.getId(), saved);
        cache.evict(KEY_ALL);
        eventProducer.publish(
                EntityType.PRODUCT,
                EventAction.CREATE,
                saved.getId(),
                saved);

        return saved;
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Product> getAll() {

        Object cached = cache.get(KEY_ALL);
        if (cached instanceof List) {
            return (List<Product>) cached;
        }

        List<Product> products = productRepository.findAll();
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

    public Product update(Long id, ProductRequest request) {

        Product product = loadById(id);
        Supplier supplier = findSupplier(request.getSupplierId());

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setReorderLevel(request.getReorderLevel());
        product.setSupplier(supplier);

        Product saved = productRepository.save(product);

        cache.put(KEY_PREFIX + saved.getId(), saved);
        cache.evict(KEY_ALL);
        eventProducer.publish(
                EntityType.PRODUCT,
                EventAction.UPDATE,
                saved.getId(),
                saved);

        return saved;
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
        return productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found"));
    }

    private Supplier findSupplier(Long supplierId) {
        return supplierRepository.findById(supplierId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Supplier not found"));
    }
}
