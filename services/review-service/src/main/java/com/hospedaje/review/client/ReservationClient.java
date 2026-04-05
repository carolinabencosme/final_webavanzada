package com.hospedaje.review.client;

import com.hospedaje.review.dto.ApiResponse;
import com.hospedaje.review.dto.ReservationDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "reservation-service")
public interface ReservationClient {
    @GetMapping("/reservations")
    ApiResponse<List<ReservationDto>> getUserReservations(
        @RequestHeader("X-User-Id") String userId,
        @RequestHeader(value = "X-User-Email", required = false) String userEmail
    );
}
