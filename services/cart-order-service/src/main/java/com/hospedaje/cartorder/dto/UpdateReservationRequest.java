package com.hospedaje.cartorder.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateReservationRequest {

    @NotNull
    private LocalDate checkIn;

    @NotNull
    private LocalDate checkOut;

    @Min(1)
    private Integer guests;
}
