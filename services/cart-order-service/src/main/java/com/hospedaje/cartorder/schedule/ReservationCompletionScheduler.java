package com.hospedaje.cartorder.schedule;

import com.hospedaje.cartorder.entity.Reservation;
import com.hospedaje.cartorder.entity.ReservationStatus;
import com.hospedaje.cartorder.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationCompletionScheduler {

    private final ReservationRepository reservationRepository;

    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void markPastStaysCompleted() {
        LocalDate today = LocalDate.now();
        List<Reservation> list = reservationRepository.findByStatusAndCheckOutBefore(ReservationStatus.CONFIRMED, today);
        for (Reservation r : list) {
            r.setStatus(ReservationStatus.COMPLETED);
        }
        reservationRepository.saveAll(list);
        if (!list.isEmpty()) {
            log.info("reservations_marked_completed count={}", list.size());
        }
    }
}
