package com.bookstore.catalog.document;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
@Document(collection = "books")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Book {
    @Id private String id;
    private String title;
    private String author;
    private String genre;
    private String description;
    private BigDecimal price;
    private String coverUrl;
    private int stock;
    private double averageRating;
    private int totalReviews;
    @Indexed(unique = true)
    private String isbn;
    private int publishedYear;
    private String language;
    private int pages;
}
