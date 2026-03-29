package com.bookstore.review.dto;
import jakarta.validation.constraints.*;
import lombok.*;
@Data @NoArgsConstructor @AllArgsConstructor
public class CreateReviewRequest {
    @NotBlank private String bookId;
    @NotBlank private String userEmail;
    @Min(1) @Max(5) private int rating;
    @Size(max = 2000) private String comment;
}
