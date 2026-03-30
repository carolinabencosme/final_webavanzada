package com.bookstore.notification.messaging;
import com.bookstore.notification.event.*;
import com.bookstore.notification.config.RabbitMQConfig;
import com.bookstore.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
@Component @RequiredArgsConstructor @Slf4j
public class NotificationListener {
    private final NotificationService notificationService;
    @RabbitListener(queues="user.registered")
    public void onUserRegistered(UserRegisteredEvent event){
        log.info("Received user.registered for {}",event.getEmail());
        notificationService.handleUserRegistered(event);}
    @RabbitListener(queues = RabbitMQConfig.ORDER_CONFIRMED_QUEUE)
    public void onOrderCompleted(OrderCompletedEvent event){
        log.info("Received order.confirmed for {}",event.getOrderNumber());
        notificationService.handleOrderCompleted(event);}
}
