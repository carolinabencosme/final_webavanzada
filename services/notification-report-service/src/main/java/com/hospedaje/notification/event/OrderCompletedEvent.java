package com.hospedaje.notification.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
    private LocalDateTime createdAt;
    private List<OrderItemInfo> items;

    private String propertyName;
    private String city;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private int nights;

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
