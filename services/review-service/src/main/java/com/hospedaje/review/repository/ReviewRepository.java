package com.hospedaje.review.repository;

import com.hospedaje.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByPropertyId(String propertyId);

    Optional<Review> findByUserIdAndPropertyId(String userId, String propertyId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.propertyId = :propertyId")
    Double findAverageRatingByPropertyId(@Param("propertyId") String propertyId);

    long countByPropertyId(String propertyId);
}
