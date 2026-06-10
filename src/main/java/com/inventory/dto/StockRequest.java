package com.inventory.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockRequest {

    @NotNull
    private Long productId;

    @NotNull
    private Integer quantity;
}