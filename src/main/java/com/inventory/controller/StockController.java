package com.inventory.controller;

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
    public ResponseEntity<Stock> save(
            @Valid @RequestBody Stock stock) {

        return ResponseEntity.ok(
                stockService.save(stock));
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