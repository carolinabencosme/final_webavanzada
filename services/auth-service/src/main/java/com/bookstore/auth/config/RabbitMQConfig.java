package com.bookstore.auth.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE = "bookstore.events";
    public static final String USER_REGISTERED_QUEUE = "user.registered";
    public static final String USER_REGISTERED_KEY = "user.registered";

    @Bean
    public TopicExchange bookstoreExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue userRegisteredQueue() {
        return new Queue(USER_REGISTERED_QUEUE, true);
    }

    @Bean
    public Binding userRegisteredBinding(Queue userRegisteredQueue, TopicExchange bookstoreExchange) {
        return BindingBuilder.bind(userRegisteredQueue).to(bookstoreExchange).with(USER_REGISTERED_KEY);
    }
}
