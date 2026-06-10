package com.inventory.service;

import com.inventory.dto.SupplierRequest;
import com.inventory.entity.Supplier;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public Supplier create(
            SupplierRequest request) {

        Supplier supplier =
                Supplier.builder()
                        .name(request.getName())
                        .email(request.getEmail())
                        .phone(request.getPhone())
                        .build();

        return supplierRepository.save(supplier);
    }

    public List<Supplier> getAll() {
        return supplierRepository.findAll();
    }

    public Supplier getById(Long id) {

        return supplierRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Supplier not found"));
    }

    public Supplier update(
            Long id,
            SupplierRequest request) {

        Supplier supplier = getById(id);

        supplier.setName(request.getName());
        supplier.setEmail(request.getEmail());
        supplier.setPhone(request.getPhone());

        return supplierRepository.save(supplier);
    }

    public void delete(Long id) {

        Supplier supplier = getById(id);

        supplierRepository.delete(supplier);
    }
}