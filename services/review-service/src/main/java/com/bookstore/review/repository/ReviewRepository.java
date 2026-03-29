package com.bookstore.review.repository;
import com.bookstore.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByBookId(String bookId);
    List<Review> findByUserId(String userId);
    Optional<Review> findByUserIdAndBookId(String userId, String bookId);
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.bookId = :bookId")
    Double findAverageRatingByBookId(String bookId);
    long countByBookId(String bookId);
}
