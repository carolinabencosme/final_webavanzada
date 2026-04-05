package com.hospedaje.cartorder.config;

import com.hospedaje.cartorder.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * One-time style backfill so legacy rows get {@code updatedAt} for admin stats (COMPLETED today).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationUpdatedAtBackfill {

    private final ReservationRepository reservationRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void backfill() {
        int n = reservationRepository.backfillUpdatedAtWhereNull();
        if (n > 0) {
            log.info("reservation_updated_at_backfill rows={}", n);
        }
    }
}
