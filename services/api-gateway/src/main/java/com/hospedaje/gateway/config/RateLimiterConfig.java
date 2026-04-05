package com.hospedaje.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * Rate limiting key: client IP (respects {@code X-Forwarded-For} first hop when present).
 */
@Configuration
public class RateLimiterConfig {

    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String forwarded = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                String first = forwarded.split(",")[0].trim();
                if (!first.isEmpty()) {
                    return Mono.just(first);
                }
            }
            String key = Optional.ofNullable(exchange.getRequest().getRemoteAddress())
                .map(a -> a.getAddress().getHostAddress())
                .orElse("unknown");
            return Mono.just(key);
        };
    }
}
