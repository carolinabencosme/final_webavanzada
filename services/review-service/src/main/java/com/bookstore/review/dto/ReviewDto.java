package com.bookstore.review.dto;
import lombok.*;
import java.time.LocalDateTime;
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ReviewDto {
    private Long id;
    private String userId, userEmail, bookId, comment;
    private int rating;
    private LocalDateTime createdAt;
}
