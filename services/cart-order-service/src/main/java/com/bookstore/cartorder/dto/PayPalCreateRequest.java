package com.bookstore.cartorder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PayPalCreateRequest {
    @NotBlank private String userEmail;
    @NotBlank private String returnUrl;
    @NotBlank private String cancelUrl;

    @JsonProperty("userId")
    private String userId;

    @AssertTrue(message = "Do not send userId in request body; identity must come from X-User-Id header")
    public boolean isUserIdAbsent() {
        return userId == null || userId.trim().isEmpty();
    }
}
