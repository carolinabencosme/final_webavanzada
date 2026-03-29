package com.bookstore.cartorder.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
@Entity @Table(name = "order_items")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne @JoinColumn(name = "order_id")
    private Order order;
    private String bookId;
    private String bookTitle;
    private String bookAuthor;
    private String coverUrl;
    private int quantity;
    private BigDecimal price;
}
