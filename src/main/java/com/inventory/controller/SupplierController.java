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
        public ResponseEntity<Supplier> save(
                        @Valid @RequestBody SupplierRequest request) {

                Supplier supplier = supplierService.save(request);

                return ResponseEntity.ok(supplier);
        }

        @GetMapping
        public ResponseEntity<List<Supplier>> getAll(@RequestParam(defaultValue = "0") int offset,
                        @RequestParam(defaultValue = "4") int limit) {

                return ResponseEntity.ok(
                                supplierService.getAll(offset, limit));
        }

        @GetMapping("/{id}")
        public ResponseEntity<Supplier> getById(
                        @PathVariable Long id) {

                return ResponseEntity.ok(
                                supplierService.getById(id));
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<String> delete(
                        @PathVariable Long id) {

                supplierService.delete(id);

                return ResponseEntity.ok(
                                "Supplier deleted successfully");
        }
}