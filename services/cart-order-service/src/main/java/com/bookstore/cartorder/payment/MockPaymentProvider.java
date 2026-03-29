package com.bookstore.cartorder.payment;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.UUID;
@Component
public class MockPaymentProvider implements PaymentProvider {
    @Override
    public String charge(String email, BigDecimal amount, String cardNumber, String cardExpiry, String cardCvc) {
        return "mock-pay-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
