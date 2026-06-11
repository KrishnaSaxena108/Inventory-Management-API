package com.inventory.service;

import com.inventory.entity.InventoryHistory;
import com.inventory.event.InventoryEvent;
import com.inventory.repository.InventoryHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryHistoryService {

    private final InventoryHistoryRepository historyRepository;

    @Transactional
    public InventoryHistory record(InventoryEvent event) {

        InventoryHistory history =
                InventoryHistory.builder()
                        .entityType(event.getEntityType())
                        .entityId(event.getEntityId())
                        .action(event.getAction())
                        .details(event.getDetails())
                        .eventTime(event.getTimestamp())
                        .recordedAt(LocalDateTime.now())
                        .build();

        return historyRepository.save(history);
    }

    @Transactional(readOnly = true)
    public List<InventoryHistory> getAll() {
        return historyRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<InventoryHistory> getByEntityType(String entityType) {
        return historyRepository
                .findByEntityTypeOrderByRecordedAtDesc(entityType);
    }

    @Transactional(readOnly = true)
    public List<InventoryHistory> getByEntity(
            String entityType,
            String entityId) {

        return historyRepository
                .findByEntityTypeAndEntityIdOrderByRecordedAtDesc(
                        entityType,
                        entityId);
    }
}
