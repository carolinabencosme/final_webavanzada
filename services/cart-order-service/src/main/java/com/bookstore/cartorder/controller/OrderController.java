package com.bookstore.cartorder.controller;

import com.bookstore.cartorder.dto.*;
import com.bookstore.cartorder.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/{userId}/checkout")
    public ResponseEntity<ApiResponse<OrderDto>> checkout(@PathVariable String userId, @Valid @RequestBody CheckoutRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Order placed", orderService.checkout(userId, req)));
    }

    @PostMapping("/{userId}/paypal/create")
    public ResponseEntity<ApiResponse<Map<String, String>>> createPayPal(@PathVariable String userId, @Valid @RequestBody PayPalCreateRequest req) {
        return ResponseEntity.ok(ApiResponse.success("OK", orderService.createPayPalOrder(userId, req)));
    }

    @PostMapping("/{userId}/paypal/capture")
    public ResponseEntity<ApiResponse<OrderDto>> capturePayPal(@PathVariable String userId, @Valid @RequestBody PayPalCaptureRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Order placed", orderService.capturePayPalOrder(userId, req)));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<List<OrderDto>>> getUserOrders(@PathVariable String userId) {
        return ResponseEntity.ok(ApiResponse.success("OK", orderService.getUserOrders(userId)));
    }

    @GetMapping("/{userId}/{orderId}")
    public ResponseEntity<ApiResponse<OrderDto>> getOrder(@PathVariable String userId, @PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.success("OK", orderService.getOrder(userId, orderId)));
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
