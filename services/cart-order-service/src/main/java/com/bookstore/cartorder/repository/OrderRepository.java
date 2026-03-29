package com.bookstore.cartorder.repository;
import com.bookstore.cartorder.entity.Order;
import com.bookstore.cartorder.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);

    List<Order> findAllByOrderByCreatedAtDesc();

    long countByStatus(OrderStatus status);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status AND o.createdAt >= :start AND o.createdAt < :end")
    long countByStatusAndCreatedAtBetween(
        @Param("status") OrderStatus status,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o WHERE o.status = 'PAID' AND o.createdAt >= :start AND o.createdAt < :end")
    BigDecimal sumPaidTotalBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT o FROM Order o WHERE o.status = 'PAID' AND o.createdAt >= :start AND o.createdAt < :end ORDER BY o.createdAt ASC")
    List<Order> findPaidBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    List<Order> findByUserIdAndStatus(String userId, OrderStatus status);

    Optional<Order> findByPaymentIdAndUserId(String paymentId, String userId);
}
