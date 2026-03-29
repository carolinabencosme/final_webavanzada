package com.bookstore.cartorder.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PayPalCaptureRequest {
    @NotBlank private String userEmail;
    /** PayPal order ID (token query param on return URL) */
    @NotBlank private String paypalOrderId;
}
