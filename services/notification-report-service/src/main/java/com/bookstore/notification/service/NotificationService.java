package com.bookstore.notification.service;
import com.bookstore.notification.document.NotificationLog;
import com.bookstore.notification.event.OrderCreatedEvent;
import com.bookstore.notification.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
@Service @RequiredArgsConstructor @Slf4j
public class NotificationService {
    private final NotificationLogRepository logRepository;
    private final EmailService emailService;

    public void processOrderCreated(OrderCreatedEvent event) {
        String subject = "Order Confirmation #" + event.getOrderId();
        StringBuilder body = new StringBuilder();
        body.append("Thank you for your order!\n\n");
        body.append("Order ID: ").append(event.getOrderId()).append("\n");
        body.append("Total: $").append(event.getTotal()).append("\n\n");
        body.append("Items:\n");
        if (event.getItems() != null) {
            event.getItems().forEach(item ->
                body.append("- ").append(item.getBookTitle()).append(" x").append(item.getQuantity())
                    .append(" = $").append(item.getPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity()))).append("\n")
            );
        }
        body.append("\nThank you for shopping with BookStore!");
        emailService.sendEmail(event.getUserEmail(), subject, body.toString());
        NotificationLog log2 = NotificationLog.builder()
            .userId(event.getUserId()).userEmail(event.getUserEmail())
            .type("ORDER_CONFIRMATION").subject(subject).body(body.toString())
            .orderId(event.getOrderId()).status("SENT").createdAt(LocalDateTime.now()).build();
        logRepository.save(log2);
        log.info("Order confirmation sent for order {}", event.getOrderId());
    }

    public List<NotificationLog> getUserNotifications(String userId) {
        return logRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
