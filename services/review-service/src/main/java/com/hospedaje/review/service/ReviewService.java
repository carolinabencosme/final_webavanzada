package com.hospedaje.review.service;

import com.hospedaje.review.client.CatalogClient;
import com.hospedaje.review.client.ReservationClient;
import com.hospedaje.review.dto.*;
import com.hospedaje.review.entity.Review;
import com.hospedaje.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReservationClient reservationClient;
    private final CatalogClient catalogClient;

    public List<ReviewDto> getPropertyReviews(String propertyId) {
        return reviewRepository.findByPropertyId(propertyId).stream().map(this::toDto).collect(Collectors.toList());
    }

    public PropertyRatingDto getPropertyRating(String propertyId) {
        Double avg = reviewRepository.findAverageRatingByPropertyId(propertyId);
        long count = reviewRepository.countByPropertyId(propertyId);
        return PropertyRatingDto.builder()
            .propertyId(propertyId)
            .averageRating(avg == null ? 0.0 : Math.round(avg * 10.0) / 10.0)
            .totalReviews((int) count)
            .build();
    }

    @Transactional
    public ReviewDto createReview(String userId, CreateReviewRequest req) {
        if (reviewRepository.findByUserIdAndPropertyId(userId, req.getPropertyId()).isPresent()) {
            throw new RuntimeException("Ya dejaste una reseña para esta propiedad");
        }
        boolean eligible = false;
        try {
            ApiResponse<List<ReservationDto>> resp = reservationClient.getUserReservations(userId, req.getUserEmail());
            if (resp != null && resp.getData() != null) {
                eligible = resp.getData().stream()
                    .anyMatch(r -> isEligibleStay(r, req.getPropertyId()));
            }
        } catch (Exception e) {
            log.warn("No se pudo verificar reservas: {}", e.getMessage());
        }
        if (!eligible) {
            throw new RuntimeException("Solo puedes reseñar después de una estadía completada en esta propiedad");
        }
        Review review = Review.builder()
            .userId(userId)
            .userEmail(req.getUserEmail())
            .propertyId(req.getPropertyId())
            .rating(req.getRating())
            .comment(req.getComment())
            .createdAt(LocalDateTime.now())
            .build();
        Review saved = reviewRepository.save(review);
        updateCatalogRating(req.getPropertyId());
        return toDto(saved);
    }

    @Transactional
    public void deleteReview(String userId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
            .filter(r -> r.getUserId().equals(userId))
            .orElseThrow(() -> new RuntimeException("Reseña no encontrada"));
        String propertyId = review.getPropertyId();
        reviewRepository.delete(review);
        updateCatalogRating(propertyId);
    }

    private void updateCatalogRating(String propertyId) {
        try {
            Double avg = reviewRepository.findAverageRatingByPropertyId(propertyId);
            long count = reviewRepository.countByPropertyId(propertyId);
            double rounded = avg == null ? 0.0 : Math.round(avg * 10.0) / 10.0;
            catalogClient.updateRating(propertyId, rounded, (int) count);
        } catch (Exception e) {
            log.warn("No se pudo actualizar el rating en catálogo: {}", e.getMessage());
        }
    }

    private ReviewDto toDto(Review r) {
        return ReviewDto.builder()
            .id(r.getId())
            .userId(r.getUserId())
            .userEmail(r.getUserEmail())
            .propertyId(r.getPropertyId())
            .rating(r.getRating())
            .comment(r.getComment())
            .createdAt(r.getCreatedAt())
            .build();
    }

    private boolean isEligibleStay(ReservationDto r, String propertyId) {
        if (r == null || !Objects.equals(propertyId, r.getPropertyId())) {
            return false;
        }
        String st = String.valueOf(r.getStatus());
        if (!"CONFIRMED".equalsIgnoreCase(st) && !"COMPLETED".equalsIgnoreCase(st)) {
            return false;
        }
        LocalDate co = r.getCheckOut();
        return co != null && !co.isAfter(LocalDate.now());
    }

    /** Completed stay: checkout on or before today */
}
