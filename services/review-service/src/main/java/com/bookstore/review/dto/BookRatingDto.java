package com.bookstore.review.dto;
import lombok.*;
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class BookRatingDto {
    private String bookId;
    private double averageRating;
    private int totalReviews;
}
