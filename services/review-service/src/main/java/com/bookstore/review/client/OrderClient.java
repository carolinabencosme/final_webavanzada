package com.bookstore.review.client;
import com.bookstore.review.dto.ApiResponse;
import com.bookstore.review.dto.OrderDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "cart-order-service")
public interface OrderClient {
    @GetMapping("/orders")
    ApiResponse<List<OrderDto>> getUserOrders(
        @RequestHeader("X-User-Id") String userId,
        @RequestHeader(value = "X-User-Email", required = false) String userEmail
    );
}
