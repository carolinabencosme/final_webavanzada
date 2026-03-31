package com.bookstore.cartorder.controller;

import com.bookstore.cartorder.config.PayPalProperties;
import com.bookstore.cartorder.exception.PayPalApiException;
import com.bookstore.cartorder.payment.PayPalClient;
import com.bookstore.cartorder.payment.PaymentProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/payments/paypal")
@RequiredArgsConstructor
@Slf4j
public class PayPalHealthController {

    private final PayPalProperties payPalProperties;
    private final PayPalClient payPalClient;
    private final PaymentProvider paymentProvider;
    private final Environment environment;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        boolean profileAllowed = isNonProductionProfileActive();
        boolean propertyAllowed = payPalProperties.isHealthEnabled();
        boolean endpointAllowed = profileAllowed || propertyAllowed;

        if (!endpointAllowed) {
            log.warn("paypal_health blocked activeProfiles={} healthEnabledProperty={}",
                Arrays.toString(environment.getActiveProfiles()), propertyAllowed);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "error", "Not Found",
                "message", "Endpoint available only when local/dev profile is active or paypal.health-enabled=true",
                "healthEnabledProperty", propertyAllowed,
                "activeProfiles", environment.getActiveProfiles()
            ));
        }

        boolean clientIdPresent = hasText(payPalProperties.getClientId());
        boolean clientSecretPresent = hasText(payPalProperties.getClientSecret());
        boolean baseUrlPresent = hasText(payPalProperties.getBaseUrl());
        boolean sdkConfigReady = payPalProperties.isEnabled() && clientIdPresent && clientSecretPresent && baseUrlPresent;

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("activeProfiles", environment.getActiveProfiles());
        details.put("healthEnabledProperty", propertyAllowed);
        String tokenCheck = "skipped";

        if (sdkConfigReady) {
            try {
                payPalClient.getAccessToken();
                tokenCheck = "ok";
                log.info("paypal_health token_check=success provider=paypal baseUrl={}", payPalProperties.getBaseUrl());
            } catch (PayPalApiException ex) {
                tokenCheck = "failed";
                details.put("reason", ex.getInternalCode());
                details.put("upstreamStatus", ex.getUpstreamStatus() != null ? ex.getUpstreamStatus().value() : null);
                details.put("upstreamSnippet", ex.getUpstreamBodySnippet());
                log.warn("paypal_health token_check=failed code={} upstreamStatus={} snippet={}",
                    ex.getInternalCode(),
                    ex.getUpstreamStatus() != null ? ex.getUpstreamStatus().value() : null,
                    ex.getUpstreamBodySnippet());
            }
        } else {
            details.put("reason", "PAYPAL_CONFIG_INCOMPLETE");
            log.info("paypal_health token_check=skipped enabled={} clientIdPresent={} clientSecretPresent={} baseUrlPresent={}",
                payPalProperties.isEnabled(), clientIdPresent, clientSecretPresent, baseUrlPresent);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("enabled", payPalProperties.isEnabled());
        response.put("provider", paymentProvider.providerName());
        response.put("clientIdPresent", clientIdPresent);
        response.put("clientSecretPresent", clientSecretPresent);
        response.put("sdkConfigReady", sdkConfigReady);
        response.put("tokenCheck", tokenCheck);
        response.put("details", details);
        return ResponseEntity.ok(response);
    }

    private boolean isNonProductionProfileActive() {
        return Arrays.stream(environment.getActiveProfiles())
            .map(String::toLowerCase)
            .anyMatch(profile -> "dev".equals(profile) || "local".equals(profile));
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
