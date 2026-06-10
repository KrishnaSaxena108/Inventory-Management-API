package com.inventory.controller;

import com.inventory.dto.StockRequest;
import com.inventory.entity.Stock;
import com.inventory.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @PostMapping
    public ResponseEntity<Stock> createStock(
            @Valid @RequestBody StockRequest request) {

        return ResponseEntity.ok(
                stockService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<Stock>> getAllStocks() {

        return ResponseEntity.ok(
                stockService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Stock> getStockById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                stockService.getById(id));
    }

    @PutMapping("/add/{productId}")
    public ResponseEntity<Stock> addStock(
            @PathVariable Long productId,
            @RequestParam Integer quantity) {

        return ResponseEntity.ok(
                stockService.addStock(
                        productId,
                        quantity));
    }

    @PutMapping("/reduce/{productId}")
    public ResponseEntity<Stock> reduceStock(
            @PathVariable Long productId,
            @RequestParam Integer quantity) {

        return ResponseEntity.ok(
                stockService.reduceStock(
                        productId,
                        quantity));
    }

    @PutMapping("/receive/{productId}")
    public ResponseEntity<Stock> receiveStock(
            @PathVariable Long productId,
            @RequestParam Integer quantity) {

        return ResponseEntity.ok(
                stockService.receiveStock(
                        productId,
                        quantity));
    }

    @GetMapping("/check/{productId}")
    public ResponseEntity<Integer> checkLevel(
            @PathVariable Long productId) {

        return ResponseEntity.ok(
                stockService.checkLevel(productId));
    }

    @GetMapping("/reorder-alerts")
    public ResponseEntity<List<String>> reorderAlerts() {

        return ResponseEntity.ok(
                stockService.reorderAlerts());
    }
}