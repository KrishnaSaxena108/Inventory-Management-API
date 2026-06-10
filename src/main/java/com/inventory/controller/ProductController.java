package com.inventory.controller;

import com.inventory.dto.ProductRequest;
import com.inventory.entity.Product;
import com.inventory.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<Product> create(
            @Valid @RequestBody ProductRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAll() {

        return ResponseEntity.ok(
                productService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                productService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {

        return ResponseEntity.ok(
                productService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(
            @PathVariable Long id) {

        productService.delete(id);

        return ResponseEntity.ok(
                "Product deleted successfully");
    }
}