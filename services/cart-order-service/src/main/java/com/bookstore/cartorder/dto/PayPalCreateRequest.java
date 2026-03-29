package com.bookstore.cartorder.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PayPalCreateRequest {
    @NotBlank private String userEmail;
    @NotBlank private String returnUrl;
    @NotBlank private String cancelUrl;
}
