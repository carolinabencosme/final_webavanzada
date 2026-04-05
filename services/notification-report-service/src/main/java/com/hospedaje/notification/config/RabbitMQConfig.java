package com.hospedaje.notification.config;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE="hospedaje.events";
    public static final String USER_REGISTERED_QUEUE="user.registered";
    public static final String ORDER_CONFIRMED_QUEUE="order.confirmed";
    public static final String ORDER_CONFIRMED_ROUTING_KEY="order.confirmed";
    public static final String ORDER_COMPLETED_LEGACY_ROUTING_KEY="order.completed";
    @Bean public TopicExchange exchange(){return new TopicExchange(EXCHANGE,true,false);}
    @Bean public Queue userRegisteredQueue(){return new Queue(USER_REGISTERED_QUEUE,true);}
    @Bean public Queue orderConfirmedQueue(){return new Queue(ORDER_CONFIRMED_QUEUE,true);}
    @Bean public Binding userRegisteredBinding(Queue userRegisteredQueue,TopicExchange exchange){
        return BindingBuilder.bind(userRegisteredQueue).to(exchange).with("user.registered");}
    @Bean public Binding orderConfirmedBinding(Queue orderConfirmedQueue,TopicExchange exchange){
        return BindingBuilder.bind(orderConfirmedQueue).to(exchange).with(ORDER_CONFIRMED_ROUTING_KEY);}
    @Bean public Binding orderCompletedLegacyBinding(Queue orderConfirmedQueue,TopicExchange exchange){
        return BindingBuilder.bind(orderConfirmedQueue).to(exchange).with(ORDER_COMPLETED_LEGACY_ROUTING_KEY);}
    @Bean public Jackson2JsonMessageConverter messageConverter(){return new Jackson2JsonMessageConverter();}
}
