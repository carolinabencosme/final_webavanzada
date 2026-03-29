package com.bookstore.notification.event;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderCreatedEvent {
    private Long orderId;
    private String userId;
    private String userEmail;
    private BigDecimal total;
    private List<OrderItemEvent> items;

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class OrderItemEvent {
        private String bookId, bookTitle;
        private int quantity;
        private BigDecimal price;
    }
}
