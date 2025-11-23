package com.local.message_api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import com.local.message_api.config.RabbitMQConfig;
import com.local.message_api.model.Square;
import com.local.message_api.model.Circle;
import com.local.message_api.model.Pentagon;

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
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.SQUARE_ROUTING_KEY, message);
            logger.info("Message published successfully to routing key: {}", RabbitMQConfig.SQUARE_ROUTING_KEY);
        } catch (Exception ex) {
            logger.error("Failed to publish message to RabbitMQ: {}", ex.getMessage(), ex);
        }
    }

    public void publishSquare(Square square) {
        logger.info("Publishing Square to RabbitMQ with routing key: {}", RabbitMQConfig.SQUARE_ROUTING_KEY);
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.SQUARE_ROUTING_KEY, square.toString());
            logger.info("Square published successfully: {}", square);
        } catch (Exception ex) {
            logger.error("Failed to publish Square: {}", ex.getMessage(), ex);
        }
    }

    public void publishCircle(Circle circle) {
        logger.info("Publishing Circle to RabbitMQ with routing key: {}", RabbitMQConfig.CIRCLE_ROUTING_KEY);
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.CIRCLE_ROUTING_KEY, circle.toString());
            logger.info("Circle published successfully: {}", circle);
        } catch (Exception ex) {
            logger.error("Failed to publish Circle: {}", ex.getMessage(), ex);
        }
    }

    public void publishPentagon(Pentagon pentagon) {
        logger.info("Publishing Pentagon to RabbitMQ with routing keys: {} and {}", 
                RabbitMQConfig.PENTAGON_ROUTING_KEY_1, RabbitMQConfig.PENTAGON_ROUTING_KEY_2);
        try {
            // Pentagon uses two routing keys, so publish twice
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.PENTAGON_ROUTING_KEY_1, pentagon.toString());
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.PENTAGON_ROUTING_KEY_2, pentagon.toString());
            logger.info("Pentagon published successfully: {}", pentagon);
        } catch (Exception ex) {
            logger.error("Failed to publish Pentagon: {}", ex.getMessage(), ex);
        }
    }

}

