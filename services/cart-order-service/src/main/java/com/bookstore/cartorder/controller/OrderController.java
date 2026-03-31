package com.bookstore.cartorder.controller;

import com.bookstore.cartorder.config.PayPalProperties;
import com.bookstore.cartorder.dto.*;
import com.bookstore.cartorder.service.OrderService;
import com.bookstore.cartorder.web.RequestIdentityResolver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final RequestIdentityResolver identityResolver;
    private final PayPalProperties payPalProperties;

    /**
     * Creates an order from the authenticated user's cart.
     * Cart policy: if order creation succeeds, the cart is cleared.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderDto>> createOrder(HttpServletRequest request) {
        RequestIdentityResolver.RequestIdentity identity = identityResolver.resolveFromHeaders(request);
        return ResponseEntity.ok(ApiResponse.success("Order created successfully",
            orderService.createOrder(identity.userId(), identity.userEmail())));
    }

    /**
     * Legacy checkout flow using card payload. This is an alternative flow to POST /orders.
     */
    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<OrderDto>> checkout(HttpServletRequest request,
                                                          @Valid @RequestBody CheckoutRequest req) {
        RequestIdentityResolver.RequestIdentity identity = identityResolver.resolveFromHeaders(request);
        String userEmail = identity.userEmail() != null ? identity.userEmail() : req.getUserEmail();
        return ResponseEntity.ok(ApiResponse.success("Order created successfully", orderService.checkout(identity.userId(), userEmail, req)));
    }

    @GetMapping({"/paypal/config", "/paypal/public-config"})
    public ResponseEntity<ApiResponse<?>> getPublicPayPalConfig() {
        if (isInvalidPayPalConfig()) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                .success(false)
                .message("PayPal configuration is invalid. Verify clientId, clientSecret, and currency.")
                .code("PAYPAL_CONFIG_INVALID")
                .build());
        }

        PayPalPublicConfigDto dto = PayPalPublicConfigDto.builder()
            .enabled(payPalProperties.isEnabled())
            .clientId(hasText(payPalProperties.getClientId()) ? payPalProperties.getClientId() : null)
            .currency(payPalProperties.getCurrency())
            .baseUrlMode(resolveBaseUrlMode(payPalProperties.getBaseUrl()))
            .provider("paypal")
            .build();

        return ResponseEntity.ok(ApiResponse.success("OK", dto));
    }

    private boolean isInvalidPayPalConfig() {
        if (!payPalProperties.isEnabled()) {
            return false;
        }
        return !hasText(payPalProperties.getClientId())
            || !hasText(payPalProperties.getClientSecret())
            || !hasText(payPalProperties.getCurrency());
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String resolveBaseUrlMode(String baseUrl) {
        if (!hasText(baseUrl)) {
            return null;
        }

        String normalized = baseUrl.toLowerCase();
        if (normalized.contains("sandbox")) {
            return "sandbox";
        }
        if (normalized.contains("live")) {
            return "live";
        }

        return null;
    }

    @PostMapping("/paypal/create")
    public ResponseEntity<ApiResponse<Map<String, String>>> createPayPal(HttpServletRequest request,
                                                                          @Valid @RequestBody PayPalCreateRequest req) {
        RequestIdentityResolver.RequestIdentity identity = identityResolver.resolveFromHeaders(request);
        String userEmail = identity.userEmail() != null ? identity.userEmail() : req.getUserEmail();
        return ResponseEntity.ok(ApiResponse.success("OK", orderService.createPayPalOrder(identity.userId(), userEmail, req)));
    }

    @PostMapping("/paypal/capture")
    public ResponseEntity<ApiResponse<OrderDto>> capturePayPal(HttpServletRequest request,
                                                                @Valid @RequestBody PayPalCaptureRequest req) {
        RequestIdentityResolver.RequestIdentity identity = identityResolver.resolveFromHeaders(request);
        String userEmail = identity.userEmail() != null ? identity.userEmail() : req.getUserEmail();
        return ResponseEntity.ok(ApiResponse.success("Order created successfully", orderService.capturePayPalOrder(identity.userId(), userEmail, req)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderDto>>> getUserOrders(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Orders fetched successfully", orderService.getUserOrders(identityResolver.resolveUserId(request))));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDto>> getOrder(HttpServletRequest request,
                                                           @PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.success("Order fetched successfully", orderService.getOrder(identityResolver.resolveUserId(request), orderId)));
    }

    @GetMapping("/admin/all")
    public ResponseEntity<ApiResponse<List<OrderDto>>> adminAllOrders() {
        return ResponseEntity.ok(ApiResponse.success("OK", orderService.getAllOrders()));
    }

    @GetMapping("/admin/stats")
    public ResponseEntity<ApiResponse<OrderStatsDto>> adminStats() {
        return ResponseEntity.ok(ApiResponse.success("OK", orderService.getDashboardStats()));
    }
}
