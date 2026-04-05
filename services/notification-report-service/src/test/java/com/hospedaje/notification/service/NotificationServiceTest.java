package com.hospedaje.notification.service;

import com.hospedaje.notification.document.InvoiceSnapshot;
import com.hospedaje.notification.repository.InvoiceSnapshotRepository;
import com.hospedaje.notification.repository.NotificationLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    @Mock
    private EmailService emailService;
    @Mock
    private InvoiceService invoiceService;
    @Mock
    private NotificationLogRepository notificationLogRepository;
    @Mock
    private InvoiceSnapshotRepository invoiceSnapshotRepository;
    @Mock
    private InvoiceAccessTokenService invoiceAccessTokenService;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void shouldLoadInvoiceFromSnapshot() {
        InvoiceSnapshot snapshot = InvoiceSnapshot.builder()
            .orderId("6")
            .orderNumber("ORD-6")
            .userId("u1")
            .userEmail("customer@bookstore.com")
            .total(BigDecimal.valueOf(11.99))
            .items(List.of())
            .build();
        when(invoiceSnapshotRepository.findByOrderId("6")).thenReturn(Optional.of(snapshot));

        var event = notificationService.getInvoiceEvent("6");

        assertEquals("ORD-6", event.getOrderNumber());
        assertEquals("customer@bookstore.com", event.getUserEmail());
    }

    @Test
    void shouldFailWhenInvoiceSnapshotIsMissing() {
        when(invoiceSnapshotRepository.findByOrderId("404")).thenReturn(Optional.empty());
        assertThrows(InvoiceNotFoundException.class, () -> notificationService.getInvoiceEvent("404"));
    }
}
