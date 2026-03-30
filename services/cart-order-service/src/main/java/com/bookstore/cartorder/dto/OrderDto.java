package com.bookstore.cartorder.dto;
import com.bookstore.cartorder.entity.OrderStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderDto {
    private Long id;
    private Long orderId;
    private String userId, userEmail, paymentId;
    private OrderStatus status;
    private BigDecimal total;
    private LocalDateTime createdAt;
    private List<CartItemDto> items;
}
