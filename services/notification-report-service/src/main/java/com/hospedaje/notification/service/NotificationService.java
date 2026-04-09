package com.hospedaje.notification.service;
import com.hospedaje.notification.document.NotificationLog;
import com.hospedaje.notification.document.InvoiceSnapshot;
import com.hospedaje.notification.event.*;
import com.hospedaje.notification.repository.InvoiceSnapshotRepository;
import com.hospedaje.notification.repository.NotificationLogRepository;
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
    private final InvoiceSnapshotRepository invoiceSnapshotRepository;
    private final InvoiceAccessTokenService invoiceAccessTokenService;

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
            persistInvoiceSnapshot(event);
            byte[] pdf = invoiceService.generateInvoice(event);
            String invoiceToken = invoiceAccessTokenService.generate(event.getOrderId());
            emailService.sendOrderConfirmationEmail(event.getUserEmail(), event.getUserEmail(), event, pdf, invoiceToken);
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
        OrderCompletedEvent event = getInvoiceEvent(orderId);
        return invoiceService.generateInvoice(event);
    }

    public OrderCompletedEvent getInvoiceEvent(String orderId) {
        InvoiceSnapshot snapshot = invoiceSnapshotRepository.findByOrderId(orderId)
            .orElseThrow(() -> new InvoiceNotFoundException(orderId));
        log.info("invoice_snapshot_loaded orderId={} orderNumber={} items={} total={}",
            orderId,
            snapshot.getOrderNumber(),
            snapshot.getItems() == null ? 0 : snapshot.getItems().size(),
            snapshot.getTotal());
        return OrderCompletedEvent.builder()
            .reservationId(snapshot.getOrderId())
            .reservationNumber(snapshot.getOrderNumber())
            .userId(snapshot.getUserId())
            .userEmail(snapshot.getUserEmail())
            .total(snapshot.getTotal())
            .createdAt(snapshot.getCreatedAt())
            .items(snapshot.getItems())
            .build();
    }

    private void persistInvoiceSnapshot(OrderCompletedEvent event) {
        InvoiceSnapshot snapshot = InvoiceSnapshot.builder()
            .orderId(event.getOrderId())
            .orderNumber(event.getOrderNumber())
            .userId(event.getUserId())
            .userEmail(event.getUserEmail())
            .total(event.getTotal())
            .createdAt(event.getCreatedAt())
            .items(event.getItems())
            .build();
        invoiceSnapshotRepository.save(snapshot);
        log.info("invoice_snapshot_saved orderId={} orderNumber={} items={} total={}",
            event.getOrderId(),
            event.getOrderNumber(),
            event.getItems() == null ? 0 : event.getItems().size(),
            event.getTotal());
    }

    public List<NotificationLog> getHistory() {
        return logRepository.findTop50ByOrderByCreatedAtDesc();
    }
}
