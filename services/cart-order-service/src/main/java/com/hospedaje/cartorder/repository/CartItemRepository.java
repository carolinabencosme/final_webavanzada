package com.hospedaje.cartorder.repository;

import com.hospedaje.cartorder.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUserId(String userId);

    Optional<CartItem> findByUserIdAndPropertyIdAndCheckInAndCheckOut(
        String userId,
        String propertyId,
        LocalDate checkIn,
        LocalDate checkOut
    );

    Optional<CartItem> findByIdAndUserId(Long id, String userId);

    void deleteByUserId(String userId);
}
