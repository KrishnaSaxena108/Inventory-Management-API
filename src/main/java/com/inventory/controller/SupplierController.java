package com.inventory.controller;

import com.inventory.dto.SupplierRequest;
import com.inventory.entity.Supplier;
import com.inventory.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping
    public Supplier create(
            @Valid @RequestBody SupplierRequest request) {

        return supplierService.create(request);
    }

    @GetMapping
    public List<Supplier> getAll() {
        return supplierService.getAll();
    }

    @GetMapping("/{id}")
    public Supplier getById(
            @PathVariable Long id) {

        return supplierService.getById(id);
    }

    @PutMapping("/{id}")
    public Supplier update(
            @PathVariable Long id,
            @Valid @RequestBody SupplierRequest request) {

        return supplierService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public String delete(
            @PathVariable Long id) {

        supplierService.delete(id);

        return "Supplier Deleted Successfully";
    }
}