package com.bookstore.notification.event;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderCompletedEvent {
    private String orderId,orderNumber,userId,userEmail;
    private BigDecimal total;
    private List<OrderItemInfo> items;
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class OrderItemInfo {
        private String bookId,bookTitle; private int quantity; private BigDecimal price;
    }
}
