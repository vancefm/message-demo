package com.local.message_api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.local.message_api.config.RabbitMQConfig;
import com.local.message_api.model.*;
import com.local.message_api.repository.OutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class MessagePublisher {
    private static final Logger logger = LoggerFactory.getLogger(MessagePublisher.class);

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public MessagePublisher(OutboxRepository outboxRepository, ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;}

    public void publishSquare(Square square) {
        logger.info("Writing Square to outbox");
        try {
            String payload = objectMapper.writeValueAsString(square);
            OutboxEvent event = new OutboxEvent(
                    UUID.randomUUID().toString(),
                    "Square",
                    payload,
                    RabbitMQConfig.SQUARE_ROUTING_KEY
            );
            outboxRepository.save(event);
            logger.info("Square saved to outbox with id: {}", event.getId());
        } catch (Exception ex) {
            logger.error("Failed to save Square to outbox: {}", ex.getMessage(), ex);
        }
    }

    public void publishCircle(Circle circle) {
        logger.info("Writing Circle to outbox");
        try {
            String payload = objectMapper.writeValueAsString(circle);
            OutboxEvent event = new OutboxEvent(
                    UUID.randomUUID().toString(),
                    "Circle",
                    payload,
                    RabbitMQConfig.CIRCLE_ROUTING_KEY
            );
            outboxRepository.save(event);
            logger.info("Circle saved to outbox with id: {}", event.getId());
        } catch (Exception ex) {
            logger.error("Failed to save Circle to outbox: {}", ex.getMessage(), ex);
        }
    }

    public void publishPentagon(Pentagon pentagon) {
        logger.info("Writing Pentagon to outbox (with 2 routing keys)");
        try {
            String payload = objectMapper.writeValueAsString(pentagon);
            
            // Pentagon publishes to two routing keys, so create two outbox events
            OutboxEvent event1 = new OutboxEvent(
                    UUID.randomUUID().toString(),
                    "Pentagon",
                    payload,
                    RabbitMQConfig.PENTAGON_ROUTING_KEY_1
            );
            OutboxEvent event2 = new OutboxEvent(
                    UUID.randomUUID().toString(),
                    "Pentagon",
                    payload,
                    RabbitMQConfig.PENTAGON_ROUTING_KEY_2
            );
            outboxRepository.save(event1);
            outboxRepository.save(event2);
            logger.info("Pentagon saved to outbox with ids: {} and {}", event1.getId(), event2.getId());
        } catch (Exception ex) {
            logger.error("Failed to save Pentagon to outbox: {}", ex.getMessage(), ex);
        }
    }

}

