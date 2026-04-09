package com.hospedaje.cartorder.repository;

import com.hospedaje.cartorder.entity.RoomUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;

public interface RoomUnitRepository extends JpaRepository<RoomUnit, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM RoomUnit u WHERE u.propertyId = :propertyId AND u.roomType = :roomType ORDER BY u.unitOrder ASC")
    List<RoomUnit> findByPropertyIdAndRoomTypeForUpdate(
        @Param("propertyId") String propertyId,
        @Param("roomType") String roomType
    );

    long countByPropertyIdAndRoomType(String propertyId, String roomType);
}
