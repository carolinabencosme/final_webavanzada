package com.bookstore.cartorder.dto;
import lombok.*;
import java.math.BigDecimal;
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CartItemDto {
    private Long id;
    private String bookId, bookTitle, bookAuthor, coverUrl;
    private int quantity;
    private BigDecimal price;
}
