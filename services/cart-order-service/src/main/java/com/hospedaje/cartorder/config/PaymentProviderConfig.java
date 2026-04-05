package com.hospedaje.cartorder.config;

import com.hospedaje.cartorder.payment.MockPaymentProvider;
import com.hospedaje.cartorder.payment.PayPalPaymentProvider;
import com.hospedaje.cartorder.payment.PayPalClient;
import com.hospedaje.cartorder.payment.PaymentProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentProviderConfig {

    @Bean
    @ConditionalOnProperty(name = "paypal.enabled", havingValue = "true")
    public PaymentProvider payPalPaymentProvider(PayPalClient payPalClient) {
        return new PayPalPaymentProvider(payPalClient);
    }

    @Bean
    @ConditionalOnProperty(name = "payment.mock", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(PaymentProvider.class)
    public PaymentProvider mockPaymentProvider() {
        return new MockPaymentProvider();
    }
}
