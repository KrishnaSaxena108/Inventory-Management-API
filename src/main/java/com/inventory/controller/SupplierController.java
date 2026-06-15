package com.inventory.controller;

import com.inventory.dto.SupplierRequest;
import com.inventory.entity.Supplier;
import com.inventory.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping
    public ResponseEntity<Supplier> create(
            @Valid @RequestBody SupplierRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(supplierService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<Supplier>> getAll(@RequestParam(defaultValue = "0") int offset,
        @RequestParam(defaultValue = "10") int limit) {

        return ResponseEntity.ok(
                supplierService.getAll(offset, limit));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Supplier> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                supplierService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Supplier> update(
            @PathVariable Long id,
            @Valid @RequestBody SupplierRequest request) {

        return ResponseEntity.ok(
                supplierService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(
            @PathVariable Long id) {

        supplierService.delete(id);

        return ResponseEntity.ok(
                "Supplier deleted successfully");
    }
}