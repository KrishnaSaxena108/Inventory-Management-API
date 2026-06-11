package com.inventory.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String entityType;

    @Column
    private String entityId;

    @Column(nullable = false)
    private String action;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String details;

    @Column
    private LocalDateTime eventTime;

    @Column(nullable = false)
    private LocalDateTime recordedAt;
}
