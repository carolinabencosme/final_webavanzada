package com.hospedaje.cartorder.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationStatsDto {
    /** New reservations created today */
    private long reservationsTodayCount;
    /** Awaiting payment */
    private long pendingPaymentCount;
    /** Confirmed (paid) today */
    private long confirmedTodayCount;
    /** Cancelled today */
    private long cancelledTodayCount;
    /** Moved to COMPLETED today (checkout processed / status update today) */
    private long completedTodayCount;
    /** Revenue from confirmed reservations today */
    private BigDecimal confirmedTodayTotal;
    /** Last 7 days confirmed revenue */
    private List<DailyTotal> last7DaysConfirmed;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyTotal {
        private String date;
        private BigDecimal total;
    }
}
