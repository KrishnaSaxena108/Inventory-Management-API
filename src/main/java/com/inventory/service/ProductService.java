package com.inventory.service;

import com.inventory.dto.ProductRequest;
import com.inventory.entity.Product;
import com.inventory.entity.Supplier;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;

    public Product create(
            ProductRequest request) {

        Supplier supplier =
                supplierRepository.findById(
                                request.getSupplierId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Supplier not found"));

        Product product =
                Product.builder()
                        .name(request.getName())
                        .description(
                                request.getDescription())
                        .reorderLevel(
                                request.getReorderLevel())
                        .supplier(supplier)
                        .build();

        return productRepository.save(product);
    }

    public List<Product> getAll() {
        return productRepository.findAll();
    }

    public Product getById(Long id) {

        return productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found"));
    }

    public Product update(
            Long id,
            ProductRequest request) {

        Product product = getById(id);

        Supplier supplier =
                supplierRepository.findById(
                                request.getSupplierId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Supplier not found"));

        product.setName(request.getName());
        product.setDescription(
                request.getDescription());
        product.setReorderLevel(
                request.getReorderLevel());
        product.setSupplier(supplier);

        return productRepository.save(product);
    }

    public void delete(Long id) {

        Product product = getById(id);

        productRepository.delete(product);
    }
}