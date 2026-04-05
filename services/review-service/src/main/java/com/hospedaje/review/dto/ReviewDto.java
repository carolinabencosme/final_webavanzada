package com.hospedaje.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDto {
    private Long id;
    private String userId;
    private String userEmail;
    private String propertyId;
    private String comment;
    private int rating;
    private LocalDateTime createdAt;
}
