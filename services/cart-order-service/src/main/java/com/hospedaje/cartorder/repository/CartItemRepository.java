package com.hospedaje.cartorder.repository;

import com.hospedaje.cartorder.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUserId(String userId);

    @Query("""
        SELECT c
        FROM CartItem c
        WHERE c.userId = :userId
          AND c.propertyId = :propertyId
          AND c.checkIn = :checkIn
          AND c.checkOut = :checkOut
        """)
    Optional<CartItem> findExistingItem(
        @Param("userId") String userId,
        @Param("propertyId") String propertyId,
        @Param("checkIn") LocalDate checkIn,
        @Param("checkOut") LocalDate checkOut
    );

    Optional<CartItem> findByIdAndUserId(Long id, String userId);

    void deleteByUserId(String userId);
}
