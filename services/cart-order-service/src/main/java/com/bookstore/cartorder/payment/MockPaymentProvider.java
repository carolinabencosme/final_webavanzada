package com.bookstore.cartorder.payment;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public class MockPaymentProvider implements PaymentProvider {
    @Override
    public String charge(String email, BigDecimal amount, String cardNumber, String cardExpiry, String cardCvc) {
        return "mock-pay-" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Override
    public Map<String, String> createOrder(BigDecimal amountUsd, String returnUrl, String cancelUrl) {
        String orderId = "mock-order-" + UUID.randomUUID().toString().substring(0, 8);
        return Map.of(
            "paypalOrderId", orderId,
            "approvalUrl", returnUrl != null ? returnUrl : "https://mock.local/approve"
        );
    }

    @Override
    public String captureOrder(String providerOrderId) {
        return "mock-cap-" + providerOrderId;
    }

    @Override
    public String providerName() {
        return "mock";
    }
}
