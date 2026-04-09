package com.hospedaje.cartorder.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemDto {
    private Long id;
    private String propertyId;
    private String propertyName;
    private String roomType;
    private String city;
    private String imageUrl;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private int quantity;
    private int guests;
    private int nights;
    private BigDecimal pricePerNight;
    private BigDecimal lineTotal;
}
