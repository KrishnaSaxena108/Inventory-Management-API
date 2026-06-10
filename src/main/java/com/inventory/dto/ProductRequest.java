package com.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    @Pattern(
            regexp = "^[A-Za-z]+(?:\\s[A-Za-z]+)*$",
            message = "Product name can contain only alphabets and spaces"
    )
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Reorder level is required")
    @Min(value = 0, message = "Reorder level cannot be negative")
    private Integer reorderLevel;

    @NotNull(message = "Supplier ID is required")
    private Long supplierId;
}