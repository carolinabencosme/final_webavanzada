package com.bookstore.cartorder.config;

import com.bookstore.cartorder.payment.MockPaymentProvider;
import com.bookstore.cartorder.payment.PayPalPaymentProvider;
import com.bookstore.cartorder.payment.PayPalClient;
import com.bookstore.cartorder.payment.PaymentProvider;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class PaymentProviderConfig {
    private final PayPalProperties payPalProperties;
    private final ObjectProvider<PaymentProvider> paymentProvider;

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

    @PostConstruct
    public void logPaymentStartupConfiguration() {
        boolean clientIdPresent = hasText(payPalProperties.getClientId());
        boolean clientSecretPresent = hasText(payPalProperties.getClientSecret());
        boolean baseUrlPresent = hasText(payPalProperties.getBaseUrl());
        PaymentProvider resolvedProvider = paymentProvider.getIfAvailable();
        String activeProvider = resolvedProvider != null ? resolvedProvider.providerName() : "none";

        log.info(
            "payment_startup paypalEnabled={} clientIdPresent={} clientSecretPresent={} baseUrlPresent={} activeProvider={}",
            payPalProperties.isEnabled(),
            clientIdPresent,
            clientSecretPresent,
            baseUrlPresent,
            activeProvider
        );
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
