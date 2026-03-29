package com.bookstore.cartorder.dto;
import jakarta.validation.constraints.*;
import lombok.*;
@Data @NoArgsConstructor @AllArgsConstructor
public class AddToCartRequest {
    @NotBlank private String bookId;
    @Min(1) private int quantity;
}
