package com.hospedaje.cartorder.dto;

import com.hospedaje.cartorder.entity.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationDto {
    private Long id;
    private Long reservationId;
    private String userId;
    private String userEmail;
    private String propertyId;
    private String propertyName;
    private String roomType;
    private String roomUnitId;
    private String city;
    private String country;
    private String imageUrl;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private int nights;
    private int guests;
    private BigDecimal pricePerNight;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal total;
    private ReservationStatus status;
    private LocalDateTime createdAt;
    private String paymentId;
    private String paypalOrderId;
    private String paypalCaptureId;
    private String paypalCaptureStatus;
    private String payerId;
    private String transactionRef;
}
