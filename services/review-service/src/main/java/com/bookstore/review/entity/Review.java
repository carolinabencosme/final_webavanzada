package com.bookstore.review.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity @Table(name = "reviews")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Review {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String userId;
    private String userEmail;
    private String bookId;
    private int rating;
    @Column(length = 2000)
    private String comment;
    private LocalDateTime createdAt;
}
