package com.inventory.service;

import com.inventory.dto.StockRequest;
import com.inventory.entity.Product;
import com.inventory.entity.Stock;
import com.inventory.exception.BadRequestException;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.StockRepository;
import com.inventory.util.InventoryValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final ProductRepository productRepository;

    public Stock create(
            StockRequest request) {

        Product product =
                productRepository.findById(
                                request.getProductId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Product not found"));

        Stock stock =
                Stock.builder()
                        .product(product)
                        .quantity(
                                request.getQuantity())
                        .lastUpdated(
                                LocalDateTime.now())
                        .build();

        return stockRepository.save(stock);
    }

    public List<Stock> getAll() {
        return stockRepository.findAll();
    }

    public Stock getById(Long id) {

        return stockRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Stock not found"));
    }

    public Stock addStock(
            Long productId,
            Integer quantity) {

        Stock stock =
                stockRepository.findByProductId(
                                productId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Stock not found"));

        stock.setQuantity(
                stock.getQuantity() + quantity);

        stock.setLastUpdated(
                LocalDateTime.now());

        return stockRepository.save(stock);
    }

    public Stock reduceStock(
            Long productId,
            Integer quantity) {

        Stock stock =
                stockRepository.findByProductId(
                                productId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Stock not found"));

        InventoryValidator
                .validateStockReduction(
                        stock,
                        quantity);

        stock.setQuantity(
                stock.getQuantity() - quantity);

        stock.setLastUpdated(
                LocalDateTime.now());

        return stockRepository.save(stock);
    }

        public Integer checkLevel(Long productId) {

        Stock stock =
                stockRepository.findByProductId(productId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Stock not found"));

        return stock.getQuantity();
    }

    public Stock receiveStock(
            Long productId,
            Integer quantity) {

        if (quantity <= 0) {
            throw new BadRequestException(
                    "Quantity must be greater than zero");
        }

        Stock stock =
                stockRepository.findByProductId(productId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Stock not found"));

        stock.setQuantity(
                stock.getQuantity() + quantity);

        stock.setLastUpdated(
                LocalDateTime.now());

        return stockRepository.save(stock);
    }

    public List<String> reorderAlerts() {

        List<String> alerts = new ArrayList<>();

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
}