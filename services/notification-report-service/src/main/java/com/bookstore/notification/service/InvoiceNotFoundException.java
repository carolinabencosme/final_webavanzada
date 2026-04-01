package com.bookstore.notification.service;

public class InvoiceNotFoundException extends RuntimeException {
    public InvoiceNotFoundException(String orderId) {
        super("Invoice not found for orderId=" + orderId);
    }
}
