package com.bookstore.review.client;
import com.bookstore.review.dto.ApiResponse;
import com.bookstore.review.dto.OrderDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@FeignClient(name = "cart-order-service")
public interface OrderClient {
    @GetMapping("/orders/{userId}")
    ApiResponse<List<OrderDto>> getUserOrders(@PathVariable String userId);
}
