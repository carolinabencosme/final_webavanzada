package com.hospedaje.review.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReservationDto {
    @JsonAlias({"id", "reservationId"})
    private Long id;
    private String userId;
    private String status;
    private String propertyId;
    private LocalDate checkOut;
}
