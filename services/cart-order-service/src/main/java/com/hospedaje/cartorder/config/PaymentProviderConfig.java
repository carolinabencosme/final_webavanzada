package com.hospedaje.cartorder.config;

import com.hospedaje.cartorder.payment.MockPaymentProvider;
import com.hospedaje.cartorder.payment.PayPalPaymentProvider;
import com.hospedaje.cartorder.payment.PayPalClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentProviderConfig {

    @Bean
    public MockPaymentProvider mockPaymentProvider() {
        return new MockPaymentProvider();
    }

    @Bean
    @ConditionalOnProperty(name = "paypal.enabled", havingValue = "true")
    public PayPalPaymentProvider payPalPaymentProvider(PayPalClient payPalClient) {
        return new PayPalPaymentProvider(payPalClient);
    }
}
