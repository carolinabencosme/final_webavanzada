package com.hospedaje.catalog.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.List;

@Document(collection = "properties")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Property {

    @Id
    private String id;

    private String name;
    private String description;

    private String city;
    private String country;
    private String address;

    /** HOTEL, APARTMENT, VILLA */
    @Indexed
    private String propertyType;

    /** e.g. Suite, Doble, Estudio */
    @Indexed
    private String roomType;

    private List<String> amenities;

    /** Primary image for cards */
    private String imageUrl;
    private List<String> images;

    @Indexed
    private BigDecimal pricePerNight;

    private int maxGuests;

    private double averageRating;
    private int totalReviews;

    @Indexed(unique = true)
    private String externalRef;
}
