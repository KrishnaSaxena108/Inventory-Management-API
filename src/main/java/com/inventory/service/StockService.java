package com.inventory.service;

import com.inventory.cache.CacheProvider;
import com.inventory.dto.StockRequest;
import com.inventory.entity.Product;
import com.inventory.entity.Stock;
import com.inventory.event.EntityType;
import com.inventory.event.EventAction;
import com.inventory.exception.BadRequestException;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.kafka.InventoryEventProducer;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.StockRepository;
import com.inventory.util.InventoryValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Stock fetch APIs are served from an in-process Caffeine cache, falling
 * back to the DAO on a cache miss or failure. Save/update APIs additionally
 * write the latest snapshot into the Redis distributed cache.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class StockService {

    private static final String KEY_PREFIX = "stock::";
    private static final String KEY_ALL = "stock::all";
    private static final String KEY_PRODUCT = "stock::product::";
    private static final String KEY_LEVEL = "stock::level::";

    private final StockRepository stockRepository;
    private final ProductRepository productRepository;
    private final InventoryEventProducer eventProducer;

    @Qualifier("caffeineCacheProvider")
    private final CacheProvider readCache;

    @Qualifier("redisCacheProvider")
    private final CacheProvider redisCache;

    public Stock create(StockRequest request) {

        Product product =
                productRepository.findById(request.getProductId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Product not found"));

        Stock stock =
                Stock.builder()
                        .product(product)
                        .quantity(request.getQuantity())
                        .lastUpdated(LocalDateTime.now())
                        .build();

        Stock saved = stockRepository.save(stock);

        cacheOnWrite(saved);
        publish(EventAction.CREATE, saved);

        return saved;
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Stock> getAll() {

        Object cached = readCache.get(KEY_ALL);
        if (cached instanceof List) {
            return (List<Stock>) cached;
        }

        List<Stock> stocks = stockRepository.findAll();
        readCache.put(KEY_ALL, stocks);
        return stocks;
    }

    @Transactional(readOnly = true)
    public Stock getById(Long id) {

        Object cached = readCache.get(KEY_PREFIX + id);
        if (cached instanceof Stock) {
            return (Stock) cached;
        }

        Stock stock = stockRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Stock not found"));
        readCache.put(KEY_PREFIX + id, stock);
        return stock;
    }

    public Stock addStock(Long productId, Integer quantity) {

        Stock stock = loadByProduct(productId);

        stock.setQuantity(stock.getQuantity() + quantity);
        stock.setLastUpdated(LocalDateTime.now());

        Stock saved = stockRepository.save(stock);

        cacheOnWrite(saved);
        publish(EventAction.STOCK_ADD, saved);

        return saved;
    }

    public Stock reduceStock(Long productId, Integer quantity) {

        Stock stock = loadByProduct(productId);

        InventoryValidator.validateStockReduction(stock, quantity);

        stock.setQuantity(stock.getQuantity() - quantity);
        stock.setLastUpdated(LocalDateTime.now());

        Stock saved = stockRepository.save(stock);

        cacheOnWrite(saved);
        publish(EventAction.STOCK_REDUCE, saved);

        return saved;
    }

    @Transactional(readOnly = true)
    public Integer checkLevel(Long productId) {

        Object cached = readCache.get(KEY_LEVEL + productId);
        if (cached instanceof Integer) {
            return (Integer) cached;
        }

        Stock stock = loadByProduct(productId);
        readCache.put(KEY_LEVEL + productId, stock.getQuantity());
        return stock.getQuantity();
    }

    public Stock receiveStock(Long productId, Integer quantity) {

        if (quantity <= 0) {
            throw new BadRequestException(
                    "Quantity must be greater than zero");
        }

        Stock stock = loadByProduct(productId);

        stock.setQuantity(stock.getQuantity() + quantity);
        stock.setLastUpdated(LocalDateTime.now());

        Stock saved = stockRepository.save(stock);

        cacheOnWrite(saved);
        publish(EventAction.STOCK_RECEIVE, saved);

        return saved;
    }

    @Transactional(readOnly = true)
    public List<String> reorderAlerts() {

        List<String> alerts = new ArrayList<>();

        List<Stock> stocks = stockRepository.findAll();

        for (Stock stock : stocks) {

            Product product = stock.getProduct();

            if (stock.getQuantity() <= product.getReorderLevel()) {

                alerts.add(
                        "REORDER REQUIRED : "
                                + product.getName()
                                + " Current Stock="
                                + stock.getQuantity()
                                + " Reorder Level="
                                + product.getReorderLevel());
            }
        }

        return alerts;
    }

    private Stock loadByProduct(Long productId) {
        return stockRepository.findByProductId(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Stock not found"));
    }

    private void cacheOnWrite(Stock saved) {

        Long productId = saved.getProduct() != null
                ? saved.getProduct().getId()
                : null;

        redisCache.put(KEY_PREFIX + saved.getId(), saved);
        readCache.put(KEY_PREFIX + saved.getId(), saved);
        readCache.evict(KEY_ALL);

        if (productId != null) {
            readCache.put(KEY_PRODUCT + productId, saved);
            readCache.put(KEY_LEVEL + productId, saved.getQuantity());
            redisCache.put(KEY_PRODUCT + productId, saved);
        }
    }

    private void publish(String action, Stock saved) {
        eventProducer.publish(
                EntityType.STOCK,
                action,
                saved.getId(),
                saved);
    }
}
