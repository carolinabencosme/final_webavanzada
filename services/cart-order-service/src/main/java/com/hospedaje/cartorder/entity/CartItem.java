package com.hospedaje.cartorder.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "cart_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;

    private String propertyId;
    private String propertyName;
    private String city;
    private String imageUrl;

    private LocalDate checkIn;
    private LocalDate checkOut;
    private int guests;
    private int nights;

    private BigDecimal pricePerNight;
    private BigDecimal lineTotal;
}
