package com.local.message_consumer.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConsumerRabbitMQConfig {

    public static final String EXCHANGE_NAME = "message.exchange";
    public static final String QUEUE_NAME = "hello.queue";
    public static final String ROUTING_KEY = "hello";

    @Bean
    public DirectExchange messageExchange() {
        return new DirectExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue helloQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public Binding binding(Queue helloQueue, DirectExchange messageExchange) {
        return BindingBuilder.bind(helloQueue)
                .to(messageExchange)
                .with(ROUTING_KEY);
    }

}
