package com.hospedaje.cartorder.dto;
import lombok.*;
import java.math.BigDecimal;
@Data @NoArgsConstructor @AllArgsConstructor
public class BookDto {
    private String id, title, author, coverUrl;
    private BigDecimal price;
    private int stock;
}
