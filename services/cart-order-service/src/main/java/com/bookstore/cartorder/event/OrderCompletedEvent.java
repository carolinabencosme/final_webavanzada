package com.bookstore.cartorder.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/** JSON shape must match notification-service OrderCompletedEvent for RabbitMQ. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCompletedEvent {
    private String orderId;
    private String orderNumber;
    private String userId;
    private String userEmail;
    private BigDecimal total;
    private List<OrderItemInfo> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemInfo {
        private String bookId;
        private String bookTitle;
        private int quantity;
        private BigDecimal price;
    }
}
