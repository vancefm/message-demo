package com.local.message_api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import com.local.message_api.config.RabbitMQConfig;

@Service
public class MessagePublisher {

    private static final Logger logger = LoggerFactory.getLogger(MessagePublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public MessagePublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishHelloMessage(String message) {
        logger.info("Publishing message to RabbitMQ: {}", message);
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, message);
            logger.info("Message published successfully to routing key: {}", RabbitMQConfig.ROUTING_KEY);
        } catch (Exception ex) {
            logger.error("Failed to publish message to RabbitMQ: {}", ex.getMessage(), ex);
            // optionally record metrics or push to a dead-letter store
        }
    }

}
