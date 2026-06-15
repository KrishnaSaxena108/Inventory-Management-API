package com.inventory.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.event.InventoryEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class InventoryEventProducer {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventProducer.class);

    private final KafkaTemplate<String, InventoryEvent> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topic:inventory-history-events}")
    private String topic;

    public void publish(
            String entityType,
            String action,
            Long entityId,
            Object snapshot) {

        InventoryEvent event = InventoryEvent.builder()
                .entityType(entityType)
                .entityId(entityId == null
                        ? null
                        : String.valueOf(entityId))
                .action(action)
                .details(serialize(snapshot))
                .timestamp(LocalDateTime.now())
                .build();

        try {
            kafkaTemplate.send(topic, event.getEntityId(), event)
                    .whenComplete((result, ex) -> {

                        if (ex == null) {

                            log.info(
                                    "KAFKA PRODUCED -> Topic={} Partition={} Offset={} Key={} Action={}",
                                    result.getRecordMetadata().topic(),
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset(),
                                    event.getEntityId(),
                                    event.getAction());

                        } else {

                            log.error(
                                    "KAFKA PRODUCER ERROR",
                                    ex);
                        }
                    });
        } catch (Exception ex) {
            log.error("Failed to publish inventory event {} {} id={}",
                    entityType, action, entityId, ex);
        }
    }

    private String serialize(Object snapshot) {

        if (snapshot == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException ex) {
            log.warn("Could not serialize event snapshot", ex);
            return String.valueOf(snapshot);
        }
    }
}
