package com.bookstore.cartorder.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
@Entity @Table(name = "cart_items")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CartItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String userId;
    private String bookId;
    private String bookTitle;
    private String bookAuthor;
    private String coverUrl;
    private int quantity;
    private BigDecimal price;
}
