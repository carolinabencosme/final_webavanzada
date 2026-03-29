package com.bookstore.review.controller;
import com.bookstore.review.dto.*;
import com.bookstore.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/reviews") @RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @GetMapping("/book/{bookId}")
    public ResponseEntity<ApiResponse<List<ReviewDto>>> getBookReviews(@PathVariable String bookId) {
        return ResponseEntity.ok(ApiResponse.success("OK", reviewService.getBookReviews(bookId)));
    }

    @GetMapping("/book/{bookId}/rating")
    public ResponseEntity<ApiResponse<BookRatingDto>> getBookRating(@PathVariable String bookId) {
        return ResponseEntity.ok(ApiResponse.success("OK", reviewService.getBookRating(bookId)));
    }

    @PostMapping("/{userId}")
    public ResponseEntity<ApiResponse<ReviewDto>> create(@PathVariable String userId, @Valid @RequestBody CreateReviewRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Review created", reviewService.createReview(userId, req)));
    }

    @DeleteMapping("/{userId}/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String userId, @PathVariable Long reviewId) {
        reviewService.deleteReview(userId, reviewId);
        return ResponseEntity.ok(ApiResponse.success("Review deleted", null));
    }
}
