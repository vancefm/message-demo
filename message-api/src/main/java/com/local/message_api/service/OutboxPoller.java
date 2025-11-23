package com.local.message_api.service;

import com.local.message_api.config.RabbitMQConfig;
import com.local.message_api.model.OutboxEvent;
import com.local.message_api.repository.OutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@EnableScheduling
public class OutboxPoller {
    private static final Logger logger = LoggerFactory.getLogger(OutboxPoller.class);

    private final OutboxRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;

    public OutboxPoller(OutboxRepository outboxRepository, RabbitTemplate rabbitTemplate) {
        this.outboxRepository = outboxRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(fixedDelay = 1000) // Poll every 1 second
    @Transactional
    public void pollAndPublish() {
        List<OutboxEvent> unpublishedEvents = outboxRepository.findByPublishedFalseOrderByCreatedAtAsc();
        
        if (unpublishedEvents.isEmpty()) {
            return;
        }

        logger.info("Found {} unpublished outbox events", unpublishedEvents.size());

        for (OutboxEvent event : unpublishedEvents) {
            try {
                // Publish to RabbitMQ
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.EXCHANGE_NAME,
                        event.getRoutingKey(),
                        event.getPayload()
                );
                
                // Mark as published
                event.setPublished(true);
                event.setPublishedAt(LocalDateTime.now());
                outboxRepository.save(event);
                
                logger.info("Successfully published outbox event id: {} with routing key: {}", 
                        event.getId(), event.getRoutingKey());
            } catch (Exception ex) {
                logger.error("Failed to publish outbox event id: {}: {}", 
                        event.getId(), ex.getMessage(), ex);
                // Event remains unpublished, will retry on next poll
            }
        }
    }
}