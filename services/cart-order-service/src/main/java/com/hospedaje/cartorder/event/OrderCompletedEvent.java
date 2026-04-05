package com.hospedaje.cartorder.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Published to RabbitMQ; shape must match notification-service OrderCompletedEvent. */
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
        /** Property id */
        private String bookId;
        /** Property / stay line description */
        private String bookTitle;
        /** Nights */
        private int quantity;
        /** Unit price per night */
        private BigDecimal price;
    }
}
