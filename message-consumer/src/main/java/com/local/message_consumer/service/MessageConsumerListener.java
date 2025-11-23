package com.local.message_consumer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class MessageConsumerListener {

    private static final Logger logger = LoggerFactory.getLogger(MessageConsumerListener.class);

    @RabbitListener(queues = "hello.queue")
    public void receiveHello(String message) {
        logger.info("Received from hello.queue: {}", message);
    }

    @RabbitListener(queues = "square.queue")
    public void receiveSquare(String message) {
        logger.info("Received Square from square.queue (routing key: shape.square): {}", message);
    }

    @RabbitListener(queues = "circle.queue")
    public void receiveCircle(String message) {
        logger.info("Received Circle from circle.queue (routing key: shape.circle): {}", message);
    }

    @RabbitListener(queues = "pentagon.queue")
    public void receivePentagon(String message) {
        logger.info("Received Pentagon from pentagon.queue (routing keys: shape.pentagon.1 or shape.pentagon.2): {}", message);
    }
}
