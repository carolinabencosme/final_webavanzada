package com.bookstore.catalog.dto;
import lombok.*;
import java.math.BigDecimal;
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class BookDto {
    private String id, title, author, genre, description, coverUrl, isbn, language;
    private BigDecimal price;
    private int stock, publishedYear, pages, totalReviews;
    private double averageRating;
}
