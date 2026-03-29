package com.bookstore.cartorder.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
@Data @NoArgsConstructor @AllArgsConstructor
public class CheckoutRequest {
    @NotBlank private String userEmail;
    private String cardNumber;
    private String cardExpiry;
    private String cardCvc;
}
