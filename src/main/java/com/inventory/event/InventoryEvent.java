package com.inventory.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryEvent {

    private String entityType;

    private String entityId;

    private String action;

    private String details;

    private LocalDateTime timestamp;
}
