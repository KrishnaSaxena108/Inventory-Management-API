package com.inventory.kafka;

import com.inventory.event.InventoryEvent;
import com.inventory.service.InventoryHistoryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;

@Component
@RequiredArgsConstructor
public class InventoryEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventConsumer.class);

    private final InventoryHistoryService historyService;

    @KafkaListener(topics = "${app.kafka.topic:inventory-history-events}", groupId = "${app.kafka.group-id:inventory-history-group}")
    public void consume(
            InventoryEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info(
                "KAFKA CONSUMED -> Topic={} Partition={} Offset={} Entity={} Action={}",
                topic,
                partition,
                offset,
                event.getEntityType(),
                event.getAction());

        historyService.record(event);
    }
}
