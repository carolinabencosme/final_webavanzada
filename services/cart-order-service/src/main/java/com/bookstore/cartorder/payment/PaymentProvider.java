package com.bookstore.cartorder.payment;

import java.math.BigDecimal;
import java.util.Map;

public interface PaymentProvider {
    String charge(String email, BigDecimal amount, String cardNumber, String cardExpiry, String cardCvc);

    Map<String, String> createOrder(BigDecimal amountUsd, String returnUrl, String cancelUrl);

    String captureOrder(String providerOrderId);

    String providerName();
}
