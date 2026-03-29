package com.bookstore.cartorder.payment;
import java.math.BigDecimal;
public interface PaymentProvider {
    String charge(String email, BigDecimal amount, String cardNumber, String cardExpiry, String cardCvc);
}
