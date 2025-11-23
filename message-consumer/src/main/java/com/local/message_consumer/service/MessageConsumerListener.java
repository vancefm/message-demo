package com.local.message_consumer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import com.local.message_consumer.config.ConsumerRabbitMQConfig;

@Service
public class MessageConsumerListener {

    private static final Logger logger = LoggerFactory.getLogger(MessageConsumerListener.class);

    @RabbitListener(queues = ConsumerRabbitMQConfig.QUEUE_NAME)
    public void receive(String message) {
        logger.info("Consumer received message: {}", message);
    }
}
