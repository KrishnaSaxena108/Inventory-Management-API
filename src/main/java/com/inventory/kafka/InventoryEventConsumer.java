package com.inventory.kafka;

import com.inventory.event.InventoryEvent;
import com.inventory.service.InventoryHistoryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryEventConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(InventoryEventConsumer.class);

    private final InventoryHistoryService historyService;

    @KafkaListener(
            topics = "${app.kafka.topic:inventory-history-events}",
            groupId = "${app.kafka.group-id:inventory-history-group}")
    public void consume(InventoryEvent event) {

        if (event == null) {
            return;
        }

        log.debug("Consuming inventory event {} {} id={}",
                event.getEntityType(),
                event.getAction(),
                event.getEntityId());

        historyService.record(event);
    }
}
