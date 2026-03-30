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
    private static final String USER_ID_HEADER = "X-User-Id";

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderDto>> checkout(@RequestHeader(USER_ID_HEADER) String userId,
                                                          @Valid @RequestBody CheckoutRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Order placed", orderService.checkout(userId, req)));
    }

    @PostMapping("/paypal/create")
    public ResponseEntity<ApiResponse<Map<String, String>>> createPayPal(@RequestHeader(USER_ID_HEADER) String userId,
                                                                          @Valid @RequestBody PayPalCreateRequest req) {
        return ResponseEntity.ok(ApiResponse.success("OK", orderService.createPayPalOrder(userId, req)));
    }

    @PostMapping("/paypal/capture")
    public ResponseEntity<ApiResponse<OrderDto>> capturePayPal(@RequestHeader(USER_ID_HEADER) String userId,
                                                                @Valid @RequestBody PayPalCaptureRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Order placed", orderService.capturePayPalOrder(userId, req)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderDto>>> getUserOrders(@RequestHeader(USER_ID_HEADER) String userId) {
        return ResponseEntity.ok(ApiResponse.success("OK", orderService.getUserOrders(userId)));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDto>> getOrder(@RequestHeader(USER_ID_HEADER) String userId,
                                                           @PathVariable Long orderId) {
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
