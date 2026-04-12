package com.hospedaje.notification.event;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@JsonIgnoreProperties(ignoreUnknown = true)
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
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OrderItemInfo {
        @JsonAlias("bookId")
        private String propertyId;
        @JsonAlias("bookTitle")
        private String propertyLabel;
        private int quantity;
        private BigDecimal price;

        @JsonProperty("bookId")
        public String getBookId() { return propertyId; }
        @JsonProperty("bookId")
        public void setBookId(String bookId) { this.propertyId = bookId; }

        @JsonProperty("bookTitle")
        public String getBookTitle() { return propertyLabel; }
        @JsonProperty("bookTitle")
        public void setBookTitle(String bookTitle) { this.propertyLabel = bookTitle; }
    }

    @JsonProperty("orderId")
    public String getOrderId() { return reservationId; }
    @JsonProperty("orderId")
    public void setOrderId(String orderId) { this.reservationId = orderId; }

    @JsonProperty("orderNumber")
    public String getOrderNumber() { return reservationNumber; }
    @JsonProperty("orderNumber")
    public void setOrderNumber(String orderNumber) { this.reservationNumber = orderNumber; }
}
