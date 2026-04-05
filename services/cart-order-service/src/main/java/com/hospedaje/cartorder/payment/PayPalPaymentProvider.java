package com.hospedaje.cartorder.payment;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@RequiredArgsConstructor
public class PayPalPaymentProvider implements PaymentProvider {
    private final PayPalClient payPalClient;

    @Override
    public String charge(String email, BigDecimal amount, String cardNumber, String cardExpiry, String cardCvc) {
        Map<String, String> order = payPalClient.createOrder(amount, "https://bookstore.local/paypal/return", "https://bookstore.local/paypal/cancel");
        return payPalClient.captureOrder(order.get("paypalOrderId"));
    }

    @Override
    public Map<String, String> createOrder(BigDecimal amountUsd, String returnUrl, String cancelUrl) {
        return payPalClient.createOrder(amountUsd, returnUrl, cancelUrl);
    }

    @Override
    public String captureOrder(String providerOrderId) {
        return payPalClient.captureOrder(providerOrderId);
    }

    @Override
    public String providerName() {
        return "paypal";
    }
}
