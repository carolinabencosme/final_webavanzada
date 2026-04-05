package com.hospedaje.cartorder.repository;

import com.hospedaje.cartorder.entity.Reservation;
import com.hospedaje.cartorder.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUserIdOrderByCreatedAtDesc(String userId);

    List<Reservation> findAllByOrderByCreatedAtDesc();

    long countByStatus(ReservationStatus status);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.status = :status AND r.createdAt >= :start AND r.createdAt < :end")
    long countByStatusAndCreatedAtBetween(
        @Param("status") ReservationStatus status,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    @Query("SELECT COALESCE(SUM(r.total), 0) FROM Reservation r WHERE r.status = 'CONFIRMED' AND r.createdAt >= :start AND r.createdAt < :end")
    BigDecimal sumConfirmedTotalBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    List<Reservation> findByUserIdAndStatus(String userId, ReservationStatus status);

    Optional<Reservation> findByPaypalOrderIdAndUserId(String paypalOrderId, String userId);

    Optional<Reservation> findByIdAndUserId(Long id, String userId);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.propertyId = :propertyId AND r.status IN ('PENDING_PAYMENT','CONFIRMED','COMPLETED') AND r.checkIn < :checkOut AND r.checkOut > :checkIn")
    long countConflicting(
        @Param("propertyId") String propertyId,
        @Param("checkIn") java.time.LocalDate checkIn,
        @Param("checkOut") java.time.LocalDate checkOut
    );

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.id <> :excludeId AND r.propertyId = :propertyId AND r.status IN ('PENDING_PAYMENT','CONFIRMED','COMPLETED') AND r.checkIn < :checkOut AND r.checkOut > :checkIn")
    long countConflictingExcluding(
        @Param("excludeId") Long excludeId,
        @Param("propertyId") String propertyId,
        @Param("checkIn") java.time.LocalDate checkIn,
        @Param("checkOut") java.time.LocalDate checkOut
    );

    @Query("SELECT DISTINCT r.propertyId FROM Reservation r WHERE r.status IN ('PENDING_PAYMENT','CONFIRMED','COMPLETED') AND r.checkIn < :checkOut AND r.checkOut > :checkIn")
    List<String> findOccupiedPropertyIds(
        @Param("checkIn") java.time.LocalDate checkIn,
        @Param("checkOut") java.time.LocalDate checkOut
    );

    List<Reservation> findByStatusAndCheckOutBefore(ReservationStatus status, java.time.LocalDate date);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.createdAt >= :start AND r.createdAt < :end")
    long countCreatedBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
