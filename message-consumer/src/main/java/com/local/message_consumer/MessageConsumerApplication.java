package com.local.message_consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;

@SpringBootApplication
@EnableRabbit
public class MessageConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MessageConsumerApplication.class, args);
    }

}
