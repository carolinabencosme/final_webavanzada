package com.hospedaje.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateReviewRequest {
    @NotBlank
    private String propertyId;
    @NotBlank
    private String userEmail;
    @Min(1)
    @Max(5)
    private int rating;
    @Size(max = 2000)
    private String comment;
}
