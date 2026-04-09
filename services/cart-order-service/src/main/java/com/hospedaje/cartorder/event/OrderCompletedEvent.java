package com.hospedaje.cartorder.event;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonAlias("orderId")
    private String reservationId;
    @JsonAlias("orderNumber")
    private String reservationNumber;
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
        @JsonAlias("bookId")
        private String propertyId;
        /** Property / stay line description */
        @JsonAlias("bookTitle")
        private String propertyLabel;
        /** Nights */
        private int quantity;
        /** Unit price per night */
        private BigDecimal price;

        /** Legacy alias for backward compatibility with existing consumers. */
        @JsonProperty("bookId")
        public String getBookId() { return propertyId; }
        @JsonProperty("bookId")
        public void setBookId(String bookId) { this.propertyId = bookId; }

        /** Legacy alias for backward compatibility with existing consumers. */
        @JsonProperty("bookTitle")
        public String getBookTitle() { return propertyLabel; }
        @JsonProperty("bookTitle")
        public void setBookTitle(String bookTitle) { this.propertyLabel = bookTitle; }
    }

    /** Legacy alias for backward compatibility with existing consumers. */
    @JsonProperty("orderId")
    public String getOrderId() { return reservationId; }
    @JsonProperty("orderId")
    public void setOrderId(String orderId) { this.reservationId = orderId; }

    /** Legacy alias for backward compatibility with existing consumers. */
    @JsonProperty("orderNumber")
    public String getOrderNumber() { return reservationNumber; }
    @JsonProperty("orderNumber")
    public void setOrderNumber(String orderNumber) { this.reservationNumber = orderNumber; }
}
