package com.inventory.util;

import com.inventory.entity.Stock;
import com.inventory.exception.BadRequestException;

public final class InventoryValidator {

    private InventoryValidator() {
    }

    public static void validateStockReduction(
            Stock stock,
            Integer quantity) {

        if (quantity <= 0) {
            throw new BadRequestException(
                    "Quantity must be greater than zero");
        }

        if (stock.getQuantity() < quantity) {
            throw new BadRequestException(
                    "Negative inventory not allowed");
        }
    }
}