package com.hospedaje.cartorder.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

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
    @Column(nullable = true)
    private Integer guests;
    @Column(nullable = true)
    private Integer nights;

    private BigDecimal pricePerNight;
    private BigDecimal lineTotal;

    @PrePersist
    @PreUpdate
    public void applySafeDefaults() {
        if ((nights == null || nights <= 0) && checkIn != null && checkOut != null) {
            long calculated = ChronoUnit.DAYS.between(checkIn, checkOut);
            if (calculated > 0) {
                nights = (int) calculated;
            }
        }
        if (guests == null || guests <= 0) {
            guests = 1;
        }
        if (lineTotal == null && pricePerNight != null && nights != null && nights > 0) {
            lineTotal = pricePerNight
                .multiply(BigDecimal.valueOf(nights))
                .setScale(2, RoundingMode.HALF_UP);
        }
    }
}
