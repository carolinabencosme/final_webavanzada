package com.hospedaje.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyRatingDto {
    private String propertyId;
    private double averageRating;
    private int totalReviews;
}
