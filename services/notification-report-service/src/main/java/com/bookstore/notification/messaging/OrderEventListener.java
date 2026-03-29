package com.bookstore.notification.messaging;
import com.bookstore.notification.config.RabbitMQConfig;
import com.bookstore.notification.event.OrderCreatedEvent;
import com.bookstore.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
@Component @RequiredArgsConstructor @Slf4j
public class OrderEventListener {
    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.ORDER_CREATED_QUEUE)
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("Received order created event for order {}", event.getOrderId());
        notificationService.processOrderCreated(event);
    }
}
