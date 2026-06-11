package com.inventory.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${app.kafka.topic:inventory-history-events}")
    private String topic;

    @Value("${app.kafka.partitions:1}")
    private int partitions;

    @Value("${app.kafka.replicas:1}")
    private short replicas;

    @Bean
    public NewTopic inventoryHistoryTopic() {
        return TopicBuilder.name(topic)
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }
}
