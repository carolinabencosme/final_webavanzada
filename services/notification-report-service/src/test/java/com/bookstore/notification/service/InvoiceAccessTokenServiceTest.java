package com.bookstore.notification.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InvoiceAccessTokenServiceTest {

    @Test
    void shouldGenerateAndValidateToken() {
        InvoiceAccessTokenService service = new InvoiceAccessTokenService("test-secret", 60);
        String token = service.generate("6");
        assertTrue(service.isValid("6", token));
    }

    @Test
    void shouldRejectTokenForAnotherOrder() {
        InvoiceAccessTokenService service = new InvoiceAccessTokenService("test-secret", 60);
        String token = service.generate("6");
        assertFalse(service.isValid("7", token));
    }
}
