package com.hospedaje.cartorder.controller;

import com.hospedaje.cartorder.config.PayPalProperties;
import com.hospedaje.cartorder.dto.*;
import com.hospedaje.cartorder.service.ReservationService;
import com.hospedaje.cartorder.web.RequestIdentityResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final RequestIdentityResolver identityResolver;
    private final PayPalProperties payPalProperties;

    @PostMapping
    public ResponseEntity<ApiResponse<ReservationDto>> createReservation(HttpServletRequest request) {
        RequestIdentityResolver.RequestIdentity identity = identityResolver.resolveFromHeaders(request);
        return ResponseEntity.ok(ApiResponse.success("Reserva creada (pendiente de pago)",
            reservationService.createReservationFromCart(identity.userId(), identity.userEmail())));
    }

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<ReservationDto>> checkout(HttpServletRequest request,
                                                                  @Valid @RequestBody CheckoutRequest req) {
        RequestIdentityResolver.RequestIdentity identity = identityResolver.resolveFromHeaders(request);
        String userEmail = identity.userEmail() != null ? identity.userEmail() : req.getUserEmail();
        return ResponseEntity.ok(ApiResponse.success("Reserva confirmada",
            reservationService.checkout(identity.userId(), userEmail, req)));
    }

    @GetMapping({"/paypal/config", "/paypal/public-config"})
    public ResponseEntity<ApiResponse<PayPalPublicConfigDto>> getPublicPayPalConfig() {
        boolean invalidConfig = isInvalidPayPalConfig();
        boolean enabledForCheckout = payPalProperties.isEnabled() && !invalidConfig;
        String availabilityMessage = null;
        if (!payPalProperties.isEnabled()) {
            availabilityMessage = "PayPal Sandbox deshabilitado (PAYPAL_ENABLED=false). Sigue disponible el pago simulado.";
        } else if (invalidConfig) {
            availabilityMessage = "PayPal habilitado pero mal configurado. Verifique credenciales y moneda.";
        }

        PayPalPublicConfigDto dto = PayPalPublicConfigDto.builder()
            .enabled(enabledForCheckout)
            .clientId(hasText(payPalProperties.getClientId()) ? payPalProperties.getClientId() : null)
            .currency(payPalProperties.getCurrency())
            .baseUrlMode(resolveBaseUrlMode(payPalProperties.getBaseUrl()))
            .provider(enabledForCheckout ? "paypal" : "mock")
            .availabilityMessage(availabilityMessage)
            .build();

        return ResponseEntity.ok(ApiResponse.success("OK", dto));
    }

    private boolean isInvalidPayPalConfig() {
        if (!payPalProperties.isEnabled()) {
            return false;
        }
        return !hasText(payPalProperties.getClientId())
            || !hasText(payPalProperties.getClientSecret())
            || !hasText(payPalProperties.getCurrency());
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String resolveBaseUrlMode(String baseUrl) {
        if (!hasText(baseUrl)) {
            return null;
        }
        String normalized = baseUrl.toLowerCase();
        if (normalized.contains("sandbox")) {
            return "sandbox";
        }
        if (normalized.contains("live")) {
            return "live";
        }
        return null;
    }

    @PostMapping("/paypal/create")
    public ResponseEntity<ApiResponse<Map<String, String>>> createPayPal(HttpServletRequest request,
                                                                          @Valid @RequestBody PayPalCreateRequest req) {
        RequestIdentityResolver.RequestIdentity identity = identityResolver.resolveFromHeaders(request);
        String userEmail = identity.userEmail() != null ? identity.userEmail() : req.getUserEmail();
        return ResponseEntity.ok(ApiResponse.success("OK", reservationService.createPayPalOrder(identity.userId(), userEmail, req)));
    }

    @PostMapping("/paypal/capture")
    public ResponseEntity<ApiResponse<ReservationDto>> capturePayPal(HttpServletRequest request,
                                                                     @Valid @RequestBody PayPalCaptureRequest req) {
        RequestIdentityResolver.RequestIdentity identity = identityResolver.resolveFromHeaders(request);
        String userEmail = identity.userEmail() != null ? identity.userEmail() : req.getUserEmail();
        return ResponseEntity.ok(ApiResponse.success("Reserva confirmada",
            reservationService.capturePayPalOrder(identity.userId(), userEmail, req)));
    }

    @GetMapping("/availability")
    public ResponseEntity<ApiResponse<Boolean>> availability(
        @RequestParam String propertyId,
        @RequestParam java.time.LocalDate checkIn,
        @RequestParam java.time.LocalDate checkOut
    ) {
        boolean ok = reservationService.isAvailable(propertyId, checkIn, checkOut);
        return ResponseEntity.ok(ApiResponse.success("OK", ok));
    }

    /** Uso interno (p. ej. catálogo vía Feign): propiedades con reserva solapada en el rango. */
    @GetMapping("/internal/occupied-property-ids")
    public List<String> occupiedPropertyIds(
        @RequestParam java.time.LocalDate checkIn,
        @RequestParam java.time.LocalDate checkOut
    ) {
        return reservationService.findOccupiedPropertyIds(checkIn, checkOut);
    }

    @PutMapping("/{reservationId}")
    public ResponseEntity<ApiResponse<ReservationDto>> updateReservation(
        HttpServletRequest request,
        @PathVariable Long reservationId,
        @Valid @RequestBody UpdateReservationRequest req
    ) {
        return ResponseEntity.ok(ApiResponse.success("Reserva actualizada",
            reservationService.updateReservation(identityResolver.resolveUserId(request), reservationId, req)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReservationDto>>> getUserReservations(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success("OK",
            reservationService.getUserReservations(identityResolver.resolveUserId(request))));
    }

    @GetMapping("/{reservationId}")
    public ResponseEntity<ApiResponse<ReservationDto>> getReservation(HttpServletRequest request,
                                                                      @PathVariable Long reservationId) {
        return ResponseEntity.ok(ApiResponse.success("OK",
            reservationService.getReservation(identityResolver.resolveUserId(request), reservationId)));
    }

    @PutMapping("/{reservationId}/cancel")
    public ResponseEntity<ApiResponse<ReservationDto>> cancel(HttpServletRequest request,
                                                              @PathVariable Long reservationId) {
        return ResponseEntity.ok(ApiResponse.success("Reserva cancelada",
            reservationService.cancelReservation(identityResolver.resolveUserId(request), reservationId)));
    }

    @DeleteMapping("/{reservationId}")
    public ResponseEntity<ApiResponse<Void>> deleteEnded(HttpServletRequest request,
                                                         @PathVariable Long reservationId) {
        reservationService.deleteEndedReservation(identityResolver.resolveUserId(request), reservationId);
        return ResponseEntity.ok(ApiResponse.success("Reserva eliminada", null));
    }

    @GetMapping("/admin/all")
    public ResponseEntity<ApiResponse<List<ReservationDto>>> adminAll(HttpServletRequest request) {
        enforceAdminAccess(request);
        return ResponseEntity.ok(ApiResponse.success("OK", reservationService.getAllReservations()));
    }

    @GetMapping("/admin/stats")
    public ResponseEntity<ApiResponse<ReservationStatsDto>> adminStats(HttpServletRequest request) {
        enforceAdminAccess(request);
        return ResponseEntity.ok(ApiResponse.success("OK", reservationService.getDashboardStats()));
    }

    private void enforceAdminAccess(HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        if (role == null || !"ADMIN".equalsIgnoreCase(role.trim())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin role required");
        }
    }
}
