package com.bookstore.notification.config;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE="bookstore.events";
    public static final String USER_REGISTERED_QUEUE="user.registered";
    public static final String ORDER_COMPLETED_QUEUE="order.completed";
    @Bean public TopicExchange exchange(){return new TopicExchange(EXCHANGE,true,false);}
    @Bean public Queue userRegisteredQueue(){return new Queue(USER_REGISTERED_QUEUE,true);}
    @Bean public Queue orderCompletedQueue(){return new Queue(ORDER_COMPLETED_QUEUE,true);}
    @Bean public Binding userRegisteredBinding(Queue userRegisteredQueue,TopicExchange exchange){
        return BindingBuilder.bind(userRegisteredQueue).to(exchange).with("user.registered");}
    @Bean public Binding orderCompletedBinding(Queue orderCompletedQueue,TopicExchange exchange){
        return BindingBuilder.bind(orderCompletedQueue).to(exchange).with("order.completed");}
    @Bean public Jackson2JsonMessageConverter messageConverter(){return new Jackson2JsonMessageConverter();}
}
