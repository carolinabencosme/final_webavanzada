package com.hospedaje.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Service
@Slf4j
public class InvoiceAccessTokenService {

    private final String secret;
    private final long ttlSeconds;

    public InvoiceAccessTokenService(
        @Value("${app.invoice-link-secret:bookstore-invoice-secret-change-me}") String secret,
        @Value("${app.invoice-link-ttl-seconds:604800}") long ttlSeconds
    ) {
        this.secret = secret;
        this.ttlSeconds = ttlSeconds;
    }

    public String generate(String orderId) {
        long expiresAt = Instant.now().getEpochSecond() + ttlSeconds;
        String payload = orderId + ":" + expiresAt;
        String signature = hmac(payload);
        return Base64.getUrlEncoder().withoutPadding()
            .encodeToString((payload + ":" + signature).getBytes(StandardCharsets.UTF_8));
    }

    public boolean isValid(String orderId, String token) {
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
            String[] parts = decoded.split(":");
            if (parts.length != 3) {
                return false;
            }
            String tokenOrderId = parts[0];
            long expiresAt = Long.parseLong(parts[1]);
            String signature = parts[2];

            if (!orderId.equals(tokenOrderId)) {
                return false;
            }
            if (Instant.now().getEpochSecond() > expiresAt) {
                return false;
            }

            String payload = tokenOrderId + ":" + expiresAt;
            String expected = hmac(payload);
            return constantTimeEquals(expected, signature);
        } catch (Exception e) {
            log.warn("Invalid invoice token for orderId={}: {}", orderId, e.getMessage());
            return false;
        }
    }

    private String hmac(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] out = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(out);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot sign invoice token", e);
        }
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
