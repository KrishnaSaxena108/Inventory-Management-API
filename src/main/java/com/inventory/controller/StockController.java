package com.inventory.controller;

import com.inventory.dto.StockRequest;
import com.inventory.entity.Stock;
import com.inventory.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @PostMapping
    public Stock create(
            @Valid @RequestBody StockRequest request) {

        return stockService.create(request);
    }

    @GetMapping
    public List<Stock> getAll() {
        return stockService.getAll();
    }

    @GetMapping("/{id}")
    public Stock getById(
            @PathVariable Long id) {

        return stockService.getById(id);
    }

    @PutMapping("/add/{productId}")
    public Stock addStock(
            @PathVariable Long productId,
            @RequestParam Integer quantity) {

        return stockService.addStock(
                productId,
                quantity);
    }

    @PutMapping("/reduce/{productId}")
    public Stock reduceStock(
            @PathVariable Long productId,
            @RequestParam Integer quantity) {

        return stockService.reduceStock(
                productId,
                quantity);
    }

    @PutMapping("/receive/{productId}")
    public Stock receiveStock(
            @PathVariable Long productId,
            @RequestParam Integer quantity) {

        return stockService.receiveStock(
                productId,
                quantity);
    }

    @GetMapping("/check/{productId}")
    public Integer checkLevel(
            @PathVariable Long productId) {

        return stockService.checkLevel(productId);
    }

    @GetMapping("/reorder-alerts")
    public List<String> reorderAlerts() {

        return stockService.reorderAlerts();
    }
}