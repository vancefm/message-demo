package com.local.message_api.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.local.message_api.service.MessagePublisher;
import com.local.message_api.model.Square;
import com.local.message_api.model.Circle;
import com.local.message_api.model.Pentagon;

@RestController
public class ShapeController {
    private static final Logger logger = LoggerFactory.getLogger(ShapeController.class);

    private final MessagePublisher messagePublisher;

    public ShapeController(MessagePublisher messagePublisher) {
        this.messagePublisher = messagePublisher;
    }

    @GetMapping("/hello")
    public String hello() {
        logger.info("Hello World!");
        String message = "Hello World! at " + System.currentTimeMillis();
        messagePublisher.publishHelloMessage(message);
        return "Message sent: " + message;
    }

    @GetMapping("/square")
    public String publishSquare() {
        logger.info("Publishing Square");
        Square square = new Square();
        messagePublisher.publishSquare(square);
        return "Square message sent with routing key 'shape.square': " + square;
    }

    @GetMapping("/circle")
    public String publishCircle() {
        logger.info("Publishing Circle");
        Circle circle = new Circle();
        messagePublisher.publishCircle(circle);
        return "Circle message sent with routing key 'shape.circle': " + circle;
    }

    @GetMapping("/pentagon")
    public String publishPentagon() {
        logger.info("Publishing Pentagon");
        Pentagon pentagon = new Pentagon();
        messagePublisher.publishPentagon(pentagon);
        return "Pentagon message sent with routing keys 'shape.pentagon.1' and 'shape.pentagon.2': " + pentagon;
    }
}
