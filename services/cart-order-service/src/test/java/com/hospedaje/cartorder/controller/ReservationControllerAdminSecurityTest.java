package com.hospedaje.cartorder.controller;

import com.hospedaje.cartorder.config.PayPalProperties;
import com.hospedaje.cartorder.dto.ReservationStatsDto;
import com.hospedaje.cartorder.service.ReservationService;
import com.hospedaje.cartorder.web.RequestIdentityResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ReservationControllerAdminSecurityTest {

    @Mock
    private ReservationService reservationService;

    @Mock
    private RequestIdentityResolver identityResolver;

    @Mock
    private PayPalProperties payPalProperties;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ReservationController controller = new ReservationController(reservationService, identityResolver, payPalProperties);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void adminAll_shouldReturnForbidden_forClientRole() throws Exception {
        mockMvc.perform(get("/reservations/admin/all").header("X-User-Role", "CLIENT"))
            .andExpect(status().isForbidden());
    }

    @Test
    void adminStats_shouldReturnForbidden_forClientRole() throws Exception {
        mockMvc.perform(get("/reservations/admin/stats").header("X-User-Role", "CLIENT"))
            .andExpect(status().isForbidden());
    }

    @Test
    void adminEndpoints_shouldAllowAdminRole() throws Exception {
        when(reservationService.getAllReservations()).thenReturn(Collections.emptyList());
        when(reservationService.getDashboardStats()).thenReturn(
            ReservationStatsDto.builder()
                .reservationsTodayCount(0)
                .confirmedTodayTotal(BigDecimal.ZERO)
                .build()
        );

        mockMvc.perform(get("/reservations/admin/all").header("X-User-Role", "ADMIN"))
            .andExpect(status().isOk());

        mockMvc.perform(get("/reservations/admin/stats").header("X-User-Role", "ADMIN"))
            .andExpect(status().isOk());
    }
}
