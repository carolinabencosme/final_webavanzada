package com.bookstore.cartorder.dto;

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
public class OrderStatsDto {
    /** Orders awaiting payment */
    private long pendingCount;
    /** PAID orders created today */
    private long paidTodayCount;
    /** Sum of PAID totals today */
    private BigDecimal paidTodayTotal;
    /** Last 7 days daily PAID totals (oldest first) */
    private List<DailyTotal> last7DaysPaid;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyTotal {
        private String date;
        private BigDecimal total;
    }
}
