package com.hospedaje.catalog.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@FeignClient(name = "reservation-service", contextId = "reservationAvailability")
public interface ReservationAvailabilityClient {

    @GetMapping("/reservations/internal/occupied-property-ids")
    List<String> getOccupiedPropertyIds(
        @RequestParam("checkIn") LocalDate checkIn,
        @RequestParam("checkOut") LocalDate checkOut
    );
}
