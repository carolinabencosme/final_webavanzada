package com.bookstore.cartorder.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;
@Data @NoArgsConstructor @AllArgsConstructor
public class AddToCartRequest {
    @NotBlank private String bookId;
    @Min(1) private int quantity;

    @JsonProperty("userId")
    private String userId;

    @AssertTrue(message = "Do not send userId in request body; identity must come from X-User-Id header")
    public boolean isUserIdAbsent() {
        return userId == null || userId.trim().isEmpty();
    }
}
