package com.hospedaje.cartorder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddToCartRequest {

    @NotBlank
    private String propertyId;

    @NotNull
    @FutureOrPresent(message = "checkIn must be today or a future date")
    private LocalDate checkIn;

    @NotNull
    private LocalDate checkOut;

    @Min(1)
    private int guests = 2;

    @JsonProperty("userId")
    private String userId;

    @AssertTrue(message = "Do not send userId in request body; identity must come from X-User-Id header")
    public boolean isUserIdAbsent() {
        return userId == null || userId.trim().isEmpty();
    }

    @AssertTrue(message = "checkOut must be after checkIn")
    public boolean isDateRangeValid() {
        return checkIn != null && checkOut != null && checkOut.isAfter(checkIn);
    }
}
