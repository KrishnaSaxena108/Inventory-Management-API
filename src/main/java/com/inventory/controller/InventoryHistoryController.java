package com.inventory.controller;

import com.inventory.entity.InventoryHistory;
import com.inventory.service.InventoryHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class InventoryHistoryController {

    private final InventoryHistoryService historyService;

    @GetMapping
    public ResponseEntity<List<InventoryHistory>> getAll() {

        return ResponseEntity.ok(
                historyService.getAll());
    }

    @GetMapping("/{entityType}")
    public ResponseEntity<List<InventoryHistory>> getByType(
            @PathVariable String entityType) {

        return ResponseEntity.ok(
                historyService.getByEntityType(
                        entityType.toUpperCase()));
    }

    @GetMapping("/{entityType}/{entityId}")
    public ResponseEntity<List<InventoryHistory>> getByEntity(
            @PathVariable String entityType,
            @PathVariable String entityId) {

        return ResponseEntity.ok(
                historyService.getByEntity(
                        entityType.toUpperCase(),
                        entityId));
    }
}
