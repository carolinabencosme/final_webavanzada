package com.bookstore.notification.service;
import com.bookstore.notification.document.NotificationLog;
import com.bookstore.notification.event.*;
import com.bookstore.notification.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
@Service @RequiredArgsConstructor @Slf4j
public class NotificationService {
    private final EmailService emailService;
    private final InvoiceService invoiceService;
    private final NotificationLogRepository logRepository;

    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("Handling user registered: {}", event.getEmail());
        emailService.sendWelcomeEmail(event.getEmail(), event.getUsername());
        logRepository.save(NotificationLog.builder()
            .type("WELCOME").userId(event.getUserId()).email(event.getEmail())
            .subject("Welcome to BookStore!").status("SENT").createdAt(LocalDateTime.now()).build());
    }

    public void handleOrderCompleted(OrderCompletedEvent event) {
        log.info("Handling order completed: {}", event.getOrderNumber());
        try {
            byte[] pdf = invoiceService.generateInvoice(event);
            emailService.sendOrderConfirmationEmail(event.getUserEmail(), event.getUserEmail(), event, pdf);
            logRepository.save(NotificationLog.builder()
                .type("ORDER_CONFIRMATION").userId(event.getUserId()).email(event.getUserEmail())
                .subject("Order Confirmation - " + event.getOrderNumber())
                .status("SENT").orderId(event.getOrderId()).createdAt(LocalDateTime.now()).build());
        } catch (Exception e) {
            log.error("Error processing order notification: {}", e.getMessage());
            logRepository.save(NotificationLog.builder()
                .type("ORDER_CONFIRMATION").userId(event.getUserId()).email(event.getUserEmail())
                .status("FAILED").orderId(event.getOrderId())
                .errorMessage(e.getMessage()).createdAt(LocalDateTime.now()).build());
        }
    }

    public byte[] getOrGenerateInvoice(String orderId) {
        OrderCompletedEvent mockEvent = new OrderCompletedEvent();
        mockEvent.setOrderId(orderId);
        mockEvent.setOrderNumber("ORD-" + orderId.substring(0, Math.min(8, orderId.length())));
        mockEvent.setUserId("");
        mockEvent.setUserEmail("customer@example.com");
        mockEvent.setTotal(java.math.BigDecimal.ZERO);
        mockEvent.setItems(List.of());
        return invoiceService.generateInvoice(mockEvent);
    }

    public List<NotificationLog> getHistory() {
        return logRepository.findTop50ByOrderByCreatedAtDesc();
    }
}
