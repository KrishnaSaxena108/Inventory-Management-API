package com.inventory.service;

import com.inventory.cache.CacheProvider;
import com.inventory.entity.Product;
import com.inventory.entity.Stock;
import com.inventory.exception.BadRequestException;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.kafka.InventoryEventProducer;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Stock> getAll() {

        Object cached = readCache.get(KEY_ALL);

        if (cached instanceof List<?>) {
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
                        new ResourceNotFoundException("Stock not found"));

        readCache.put(KEY_PREFIX + id, stock);

        return stock;
    }

    public Stock save(Stock stock) {

        Product product = productRepository.findById(
                stock.getProduct().getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found"));

        Stock saved;

        if (stock.getId() != null &&
                stockRepository.existsById(stock.getId())) {

            Stock existing = stockRepository.findById(stock.getId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Stock not found"));

            existing.setQuantity(stock.getQuantity());
            existing.setProduct(product);
            existing.setLastUpdated(LocalDateTime.now());

            saved = stockRepository.save(existing);

            eventProducer.publish(
                    "STOCK",
                    "UPDATE",
                    saved.getId(),
                    saved);

            System.out.println(
                    "KAFKA PRODUCED -> STOCK UPDATE -> " +
                            saved.getId());

        } else {

            stock.setProduct(product);
            stock.setLastUpdated(LocalDateTime.now());

            saved = stockRepository.save(stock);

            eventProducer.publish(
                    "STOCK",
                    "CREATE",
                    saved.getId(),
                    saved);

            System.out.println(
                    "KAFKA PRODUCED -> STOCK CREATE -> " +
                            saved.getId());
        }

        cacheOnWrite(saved);

        return saved;
    }

    public Stock addStock(Long productId, Integer quantity) {

        Stock stock = loadByProduct(productId);

        stock.setQuantity(
                stock.getQuantity() + quantity);

        stock.setLastUpdated(
                LocalDateTime.now());

        Stock saved =
                stockRepository.save(stock);

        cacheOnWrite(saved);

        eventProducer.publish(
                "STOCK",
                "ADD",
                saved.getId(),
                saved);

        return saved;
    }

    public Stock reduceStock(Long productId,
                             Integer quantity) {

        Stock stock = loadByProduct(productId);

        if (stock.getQuantity() < quantity) {
            throw new BadRequestException(
                    "Insufficient stock");
        }

        stock.setQuantity(
                stock.getQuantity() - quantity);

        stock.setLastUpdated(
                LocalDateTime.now());

        Stock saved =
                stockRepository.save(stock);

        cacheOnWrite(saved);

        eventProducer.publish(
                "STOCK",
                "REDUCE",
                saved.getId(),
                saved);

        return saved;
    }

    public Stock receiveStock(Long productId,
                              Integer quantity) {

        if (quantity <= 0) {
            throw new BadRequestException(
                    "Quantity must be greater than zero");
        }

        Stock stock = loadByProduct(productId);

        stock.setQuantity(
                stock.getQuantity() + quantity);

        stock.setLastUpdated(
                LocalDateTime.now());

        Stock saved =
                stockRepository.save(stock);

        cacheOnWrite(saved);

        eventProducer.publish(
                "STOCK",
                "RECEIVE",
                saved.getId(),
                saved);

        return saved;
    }

    @Transactional(readOnly = true)
    public Integer checkLevel(Long productId) {

        Object cached =
                readCache.get(KEY_LEVEL + productId);

        if (cached instanceof Integer) {
            return (Integer) cached;
        }

        Stock stock =
                loadByProduct(productId);

        readCache.put(
                KEY_LEVEL + productId,
                stock.getQuantity());

        return stock.getQuantity();
    }

    @Transactional(readOnly = true)
    public List<String> reorderAlerts() {

        List<String> alerts =
                new ArrayList<>();

        List<Stock> stocks =
                stockRepository.findAll();

        for (Stock stock : stocks) {

            Product product =
                    stock.getProduct();

            if (stock.getQuantity()
                    <= product.getReorderLevel()) {

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

        Long productId =
                saved.getProduct().getId();

        redisCache.put(
                KEY_PREFIX + saved.getId(),
                saved);

        readCache.put(
                KEY_PREFIX + saved.getId(),
                saved);

        readCache.evict(KEY_ALL);

        readCache.put(
                KEY_PRODUCT + productId,
                saved);

        readCache.put(
                KEY_LEVEL + productId,
                saved.getQuantity());

        redisCache.put(
                KEY_PRODUCT + productId,
                saved);
    }
}