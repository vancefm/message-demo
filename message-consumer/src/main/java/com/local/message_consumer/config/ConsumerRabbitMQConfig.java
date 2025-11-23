package com.local.message_consumer.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConsumerRabbitMQConfig {

    // Exchange
    public static final String EXCHANGE_NAME = "shapes.exchange";

    // Queues
    public static final String HELLO_QUEUE = "hello.queue";
    public static final String SQUARE_QUEUE = "square.queue";
    public static final String CIRCLE_QUEUE = "circle.queue";
    public static final String PENTAGON_QUEUE = "pentagon.queue";

    // Routing Keys
    public static final String HELLO_ROUTING_KEY = "hello";
    public static final String SQUARE_ROUTING_KEY = "shape.square";
    public static final String CIRCLE_ROUTING_KEY = "shape.circle";
    public static final String PENTAGON_ROUTING_KEY_1 = "shape.pentagon.1";
    public static final String PENTAGON_ROUTING_KEY_2 = "shape.pentagon.2";

    @Bean
    public DirectExchange shapesExchange() {
        return new DirectExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue helloQueue() {
        return new Queue(HELLO_QUEUE, true);
    }

    @Bean
    public Queue squareQueue() {
        return new Queue(SQUARE_QUEUE, true);
    }

    @Bean
    public Queue circleQueue() {
        return new Queue(CIRCLE_QUEUE, true);
    }

    @Bean
    public Queue pentagonQueue() {
        return new Queue(PENTAGON_QUEUE, true);
    }

    @Bean
    public Binding helloBinding(@Qualifier("helloQueue") Queue helloQueue, DirectExchange shapesExchange) {
        return BindingBuilder.bind(helloQueue)
                .to(shapesExchange)
                .with(HELLO_ROUTING_KEY);
    }

    @Bean
    public Binding squareBinding(@Qualifier("squareQueue") Queue squareQueue, DirectExchange shapesExchange) {
        return BindingBuilder.bind(squareQueue)
                .to(shapesExchange)
                .with(SQUARE_ROUTING_KEY);
    }

    @Bean
    public Binding circleBinding(@Qualifier("circleQueue") Queue circleQueue, DirectExchange shapesExchange) {
        return BindingBuilder.bind(circleQueue)
                .to(shapesExchange)
                .with(CIRCLE_ROUTING_KEY);
    }

    @Bean
    public Binding pentagonBinding1(@Qualifier("pentagonQueue") Queue pentagonQueue, DirectExchange shapesExchange) {
        return BindingBuilder.bind(pentagonQueue)
                .to(shapesExchange)
                .with(PENTAGON_ROUTING_KEY_1);
    }

    @Bean
    public Binding pentagonBinding2(@Qualifier("pentagonQueue") Queue pentagonQueue, DirectExchange shapesExchange) {
        return BindingBuilder.bind(pentagonQueue)
                .to(shapesExchange)
                .with(PENTAGON_ROUTING_KEY_2);
    }
}
