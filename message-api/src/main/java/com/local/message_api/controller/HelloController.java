package com.local.message_api.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.local.message_api.service.MessagePublisher;

@RestController
public class HelloController {
    private static final Logger logger = LoggerFactory.getLogger(HelloController.class);

    private final MessagePublisher messagePublisher;

    public HelloController(MessagePublisher messagePublisher) {
        this.messagePublisher = messagePublisher;
    }

    @GetMapping("/hello")
    public String hello() {
        logger.info("Hello World!");
        String message = "Hello World! at " + System.currentTimeMillis();
        messagePublisher.publishHelloMessage(message);
        return "Message sent: " + message;
    }
}
