package com.inventory.service;

import com.inventory.entity.InventoryHistory;
import com.inventory.event.InventoryEvent;
import com.inventory.repository.GenericRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryHistoryService {

    private final GenericRepository<InventoryHistory> historyRepository;

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

        return historyRepository.findAll(
                InventoryHistory.class);
    }

    @Transactional(readOnly = true)
    public List<InventoryHistory> getByEntityType(
            String entityType) {

        List<InventoryHistory> history =
                historyRepository.findByField(
                        InventoryHistory.class,
                        "entityType",
                        entityType);

        history.sort(
                Comparator.comparing(
                        InventoryHistory::getRecordedAt)
                        .reversed());

        return history;
    }

    @Transactional(readOnly = true)
    public List<InventoryHistory> getByEntity(
            String entityType,
            String entityId) {

        List<InventoryHistory> history =
                historyRepository.findByTwoFields(
                        InventoryHistory.class,
                        "entityType",
                        entityType,
                        "entityId",
                        entityId);

        history.sort(
                Comparator.comparing(
                        InventoryHistory::getRecordedAt)
                        .reversed());

        return history;
    }
}