package com.hospedaje.cartorder.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;
    private String userEmail;

    private String propertyId;
    private String propertyName;
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

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    private LocalDateTime createdAt;

    private String paymentId;
    private String paypalOrderId;
    private String paypalCaptureId;
    private String paypalCaptureStatus;
    private String payerId;
    private String transactionRef;
}
