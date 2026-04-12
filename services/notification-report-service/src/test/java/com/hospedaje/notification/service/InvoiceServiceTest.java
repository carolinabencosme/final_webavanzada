package com.hospedaje.notification.service;

import com.hospedaje.notification.event.OrderCompletedEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InvoiceServiceTest {

    @Test
    void generatesPdfWhenItemsListIsEmptyUsingFallbackLine() {
        InvoiceService svc = new InvoiceService();
        OrderCompletedEvent event = OrderCompletedEvent.builder()
            .orderId("99")
            .orderNumber("RES-99")
            .userEmail("guest@test.com")
            .total(new BigDecimal("373.93"))
            .nights(5)
            .propertyName("Apartamento prueba — Punta Cana")
            .items(Collections.emptyList())
            .build();

        byte[] pdf = svc.generateInvoice(event);

        assertNotNull(pdf);
        assertTrue(pdf.length > 200, "Expected non-trivial PDF bytes");
    }
}
