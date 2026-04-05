package com.hospedaje.review.controller;

import com.hospedaje.review.dto.ApiResponse;
import com.hospedaje.review.dto.CreateReviewRequest;
import com.hospedaje.review.dto.PropertyRatingDto;
import com.hospedaje.review.dto.ReviewDto;
import com.hospedaje.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/property/{propertyId}")
    public ResponseEntity<ApiResponse<List<ReviewDto>>> getPropertyReviews(@PathVariable String propertyId) {
        return ResponseEntity.ok(ApiResponse.success("OK", reviewService.getPropertyReviews(propertyId)));
    }

    @GetMapping("/property/{propertyId}/rating")
    public ResponseEntity<ApiResponse<PropertyRatingDto>> getPropertyRating(@PathVariable String propertyId) {
        return ResponseEntity.ok(ApiResponse.success("OK", reviewService.getPropertyRating(propertyId)));
    }

    @PostMapping("/{userId}")
    public ResponseEntity<ApiResponse<ReviewDto>> create(@PathVariable String userId, @Valid @RequestBody CreateReviewRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Reseña creada", reviewService.createReview(userId, req)));
    }

    @DeleteMapping("/{userId}/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String userId, @PathVariable Long reviewId) {
        reviewService.deleteReview(userId, reviewId);
        return ResponseEntity.ok(ApiResponse.success("Reseña eliminada", null));
    }
}
