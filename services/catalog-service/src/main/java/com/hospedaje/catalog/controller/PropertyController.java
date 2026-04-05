package com.hospedaje.catalog.controller;

import com.hospedaje.catalog.dto.ApiResponse;
import com.hospedaje.catalog.dto.PageResponse;
import com.hospedaje.catalog.dto.PropertyDto;
import com.hospedaje.catalog.service.PropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;

    @GetMapping("/properties")
    public ResponseEntity<ApiResponse<PageResponse<PropertyDto>>> list(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "12") int size,
        @RequestParam(required = false) String city,
        @RequestParam(required = false) String propertyType,
        @RequestParam(required = false) String roomType,
        @RequestParam(required = false) BigDecimal minPrice,
        @RequestParam(required = false) BigDecimal maxPrice,
        @RequestParam(required = false) String q,
        @RequestParam(required = false) LocalDate checkIn,
        @RequestParam(required = false) LocalDate checkOut
    ) {
        return ResponseEntity.ok(ApiResponse.success("OK",
            propertyService.listProperties(page, size, city, propertyType, roomType, minPrice, maxPrice, q, checkIn, checkOut)));
    }

    @GetMapping("/properties/{id}")
    public ResponseEntity<ApiResponse<PropertyDto>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success("OK", propertyService.getById(id)));
    }

    @PostMapping("/properties/batch")
    public ResponseEntity<ApiResponse<List<PropertyDto>>> batch(@RequestBody List<String> ids) {
        return ResponseEntity.ok(ApiResponse.success("OK", propertyService.getByIds(ids)));
    }

    @PutMapping("/properties/{id}/rating")
    public ResponseEntity<ApiResponse<Void>> updateRating(
        @PathVariable String id,
        @RequestParam double averageRating,
        @RequestParam int totalReviews
    ) {
        propertyService.updatePropertyRating(id, averageRating, totalReviews);
        return ResponseEntity.ok(ApiResponse.success("Rating updated", null));
    }
}
