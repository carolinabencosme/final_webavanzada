package com.bookstore.review.service;
import com.bookstore.review.client.CatalogClient;
import com.bookstore.review.client.OrderClient;
import com.bookstore.review.dto.*;
import com.bookstore.review.entity.Review;
import com.bookstore.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
@Service @RequiredArgsConstructor @Slf4j
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final OrderClient orderClient;
    private final CatalogClient catalogClient;

    public List<ReviewDto> getBookReviews(String bookId) {
        return reviewRepository.findByBookId(bookId).stream().map(this::toDto).collect(Collectors.toList());
    }

    public BookRatingDto getBookRating(String bookId) {
        Double avg = reviewRepository.findAverageRatingByBookId(bookId);
        long count = reviewRepository.countByBookId(bookId);
        return BookRatingDto.builder().bookId(bookId)
            .averageRating(avg == null ? 0.0 : Math.round(avg * 10.0) / 10.0)
            .totalReviews((int) count).build();
    }

    @Transactional
    public ReviewDto createReview(String userId, CreateReviewRequest req) {
        if (reviewRepository.findByUserIdAndBookId(userId, req.getBookId()).isPresent()) {
            throw new RuntimeException("You have already reviewed this book");
        }
        boolean hasPurchased = false;
        try {
            ApiResponse<List<OrderDto>> resp = orderClient.getUserOrders(userId);
            if (resp != null && resp.getData() != null) {
                hasPurchased = resp.getData().stream()
                    .anyMatch(o -> "PAID".equals(o.getStatus()) && o.getItems() != null &&
                        o.getItems().stream().anyMatch(i -> req.getBookId().equals(i.getBookId())));
            }
        } catch (Exception e) { log.warn("Could not verify purchase: {}", e.getMessage()); }
        if (!hasPurchased) throw new RuntimeException("You must purchase this book before reviewing it");
        Review review = Review.builder().userId(userId).userEmail(req.getUserEmail())
            .bookId(req.getBookId()).rating(req.getRating()).comment(req.getComment())
            .createdAt(LocalDateTime.now()).build();
        Review saved = reviewRepository.save(review);
        updateCatalogRating(req.getBookId());
        return toDto(saved);
    }

    @Transactional
    public void deleteReview(String userId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
            .filter(r -> r.getUserId().equals(userId))
            .orElseThrow(() -> new RuntimeException("Review not found"));
        String bookId = review.getBookId();
        reviewRepository.delete(review);
        updateCatalogRating(bookId);
    }

    private void updateCatalogRating(String bookId) {
        try {
            Double avg = reviewRepository.findAverageRatingByBookId(bookId);
            long count = reviewRepository.countByBookId(bookId);
            double rounded = avg == null ? 0.0 : Math.round(avg * 10.0) / 10.0;
            catalogClient.updateRating(bookId, rounded, (int) count);
        } catch (Exception e) { log.warn("Could not update catalog rating: {}", e.getMessage()); }
    }

    private ReviewDto toDto(Review r) {
        return ReviewDto.builder().id(r.getId()).userId(r.getUserId()).userEmail(r.getUserEmail())
            .bookId(r.getBookId()).rating(r.getRating()).comment(r.getComment())
            .createdAt(r.getCreatedAt()).build();
    }
}
