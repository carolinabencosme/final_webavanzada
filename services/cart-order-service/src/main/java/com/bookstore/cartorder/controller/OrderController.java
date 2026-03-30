package com.bookstore.cartorder.controller;

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

    @PostMapping
    public ResponseEntity<ApiResponse<OrderDto>> checkout(HttpServletRequest request,
                                                          @Valid @RequestBody CheckoutRequest req) {
        RequestIdentityResolver.RequestIdentity identity = identityResolver.resolveFromHeaders(request);
        String userEmail = identity.userEmail() != null ? identity.userEmail() : req.getUserEmail();
        return ResponseEntity.ok(ApiResponse.success("Order placed", orderService.checkout(identity.userId(), userEmail, req)));
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
        return ResponseEntity.ok(ApiResponse.success("Order placed", orderService.capturePayPalOrder(identity.userId(), userEmail, req)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderDto>>> getUserOrders(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success("OK", orderService.getUserOrders(identityResolver.resolveUserId(request))));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDto>> getOrder(HttpServletRequest request,
                                                           @PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.success("OK", orderService.getOrder(identityResolver.resolveUserId(request), orderId)));
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
