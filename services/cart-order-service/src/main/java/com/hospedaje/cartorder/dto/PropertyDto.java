package com.hospedaje.cartorder.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/** Subset of catalog PropertyDto for Feign deserialization */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyDto {
    private String id;
    private String name;
    private String description;
    private String city;
    private String country;
    private String address;
    private String propertyType;
    private String roomType;
    private List<String> amenities;
    private String imageUrl;
    private List<String> images;
    private BigDecimal pricePerNight;
    private int maxGuests;
    private double averageRating;
    private int totalReviews;
}
