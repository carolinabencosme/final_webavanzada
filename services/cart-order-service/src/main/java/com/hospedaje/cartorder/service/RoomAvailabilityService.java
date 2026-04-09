package com.hospedaje.cartorder.service;

import com.hospedaje.cartorder.entity.RoomUnit;
import com.hospedaje.cartorder.repository.ReservationRepository;
import com.hospedaje.cartorder.repository.RoomUnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomAvailabilityService {

    private final RoomUnitRepository roomUnitRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    public String allocateAvailableRoomUnitId(String propertyId, String roomType, LocalDate checkIn, LocalDate checkOut, Long excludingReservationId) {
        String resolvedType = normalizeRoomType(roomType);
        List<RoomUnit> units = roomUnitRepository.findByPropertyIdAndRoomTypeForUpdate(propertyId, resolvedType);
        if (units.isEmpty()) {
            RoomUnit seeded = roomUnitRepository.save(RoomUnit.builder()
                .propertyId(propertyId)
                .roomType(resolvedType)
                .unitOrder(1)
                .unitCode(buildUnitCode(propertyId, resolvedType, 1))
                .build());
            units = List.of(seeded);
        }

        for (RoomUnit unit : units) {
            long conflicts = excludingReservationId == null
                ? reservationRepository.countConflictingByRoomUnitId(unit.getUnitCode(), checkIn, checkOut)
                : reservationRepository.countConflictingByRoomUnitIdExcluding(excludingReservationId, unit.getUnitCode(), checkIn, checkOut);
            if (conflicts == 0) {
                return unit.getUnitCode();
            }
        }
        return null;
    }

    @Transactional
    public boolean isRoomTypeAvailable(String propertyId, String roomType, LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            return false;
        }
        return allocateAvailableRoomUnitId(propertyId, roomType, checkIn, checkOut, null) != null;
    }

    public String normalizeRoomType(String roomType) {
        if (roomType == null || roomType.trim().isEmpty()) {
            return "STANDARD";
        }
        return roomType.trim().toUpperCase();
    }

    private static String buildUnitCode(String propertyId, String roomType, int unitOrder) {
        return propertyId + "::" + roomType + "::" + unitOrder;
    }
}
