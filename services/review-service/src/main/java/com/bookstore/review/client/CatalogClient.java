package com.bookstore.review.client;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
@FeignClient(name = "catalog-service")
public interface CatalogClient {
    @PutMapping("/books/{id}/rating")
    void updateRating(@PathVariable String id, @RequestParam double averageRating, @RequestParam int totalReviews);
}
