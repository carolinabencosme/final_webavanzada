package com.hospedaje.review.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "catalog-service")
public interface CatalogClient {
    @PutMapping("/properties/{id}/rating")
    void updateRating(
        @PathVariable String id,
        @RequestParam double averageRating,
        @RequestParam int totalReviews
    );
}
