package com.inventory.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SupplierRequest {

    private Long id;

    @NotBlank(message = "Supplier name is required")
    @Pattern(
            regexp = "^[A-Za-z]+(?:\\s[A-Za-z]+)*$",
            message = "Supplier name can contain only alphabets and spaces"
    )
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^[0-9]{10}$",
            message = "Phone number must contain exactly 10 digits"
    )
    private String phone;
}