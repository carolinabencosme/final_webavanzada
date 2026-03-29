package com.bookstore.cartorder.controller;
import com.bookstore.cartorder.dto.*;
import com.bookstore.cartorder.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/orders") @RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/{userId}/checkout")
    public ResponseEntity<ApiResponse<OrderDto>> checkout(@PathVariable String userId, @Valid @RequestBody CheckoutRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Order placed", orderService.checkout(userId, req)));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<List<OrderDto>>> getUserOrders(@PathVariable String userId) {
        return ResponseEntity.ok(ApiResponse.success("OK", orderService.getUserOrders(userId)));
    }

    @GetMapping("/{userId}/{orderId}")
    public ResponseEntity<ApiResponse<OrderDto>> getOrder(@PathVariable String userId, @PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.success("OK", orderService.getOrder(userId, orderId)));
    }
}
