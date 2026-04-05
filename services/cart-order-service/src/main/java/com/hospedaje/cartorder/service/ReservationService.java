package com.hospedaje.cartorder.service;

import com.hospedaje.cartorder.config.RabbitMQConfig;
import com.hospedaje.cartorder.dto.*;
import com.hospedaje.cartorder.entity.CartItem;
import com.hospedaje.cartorder.entity.Reservation;
import com.hospedaje.cartorder.entity.ReservationStatus;
import com.hospedaje.cartorder.event.OrderCompletedEvent;
import com.hospedaje.cartorder.exception.PayPalApiException;
import com.hospedaje.cartorder.payment.PaymentProvider;
import com.hospedaje.cartorder.repository.CartItemRepository;
import com.hospedaje.cartorder.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.10");

    private final ReservationRepository reservationRepository;
    private final CartItemRepository cartItemRepository;
    private final PaymentProvider paymentProvider;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public ReservationDto createReservationFromCart(String userId, String userEmail) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("No hay ítems en la selección");
        }
        if (cartItems.size() > 1) {
            throw new RuntimeException("Solo se permite una propiedad por reserva. Vacía el carrito e intenta de nuevo.");
        }
        CartItem ci = cartItems.get(0);
        assertAvailable(ci.getPropertyId(), ci.getCheckIn(), ci.getCheckOut());

        BigDecimal[] amounts = computeAmounts(ci.getPricePerNight(), ci.getNights());
        String resolvedEmail = requireUserEmail(userEmail);

        Reservation r = Reservation.builder()
            .userId(userId)
            .userEmail(resolvedEmail)
            .propertyId(ci.getPropertyId())
            .propertyName(ci.getPropertyName())
            .city(ci.getCity())
            .country(null)
            .imageUrl(ci.getImageUrl())
            .checkIn(ci.getCheckIn())
            .checkOut(ci.getCheckOut())
            .nights(ci.getNights())
            .guests(ci.getGuests())
            .pricePerNight(ci.getPricePerNight())
            .subtotal(amounts[0])
            .taxAmount(amounts[1])
            .total(amounts[2])
            .status(ReservationStatus.PENDING_PAYMENT)
            .createdAt(LocalDateTime.now())
            .paymentId("pending")
            .build();

        Reservation saved = reservationRepository.save(r);
        cartItemRepository.deleteByUserId(userId);
        return toDto(saved);
    }

    @Transactional
    public ReservationDto checkout(String userId, String userEmail, CheckoutRequest req) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("No hay ítems en la selección");
        }
        CartItem ci = cartItems.get(0);
        assertAvailable(ci.getPropertyId(), ci.getCheckIn(), ci.getCheckOut());

        BigDecimal[] amounts = computeAmounts(ci.getPricePerNight(), ci.getNights());
        String resolvedEmail = requireUserEmail(userEmail);
        log.info("payment_provider_selected flow=checkout provider={}", paymentProvider.providerName());
        String paymentId = paymentProvider.charge(resolvedEmail, amounts[2], req.getCardNumber(), req.getCardExpiry(), req.getCardCvc());
        return finalizePaidReservation(userId, resolvedEmail, ci, amounts, paymentId);
    }

    @Transactional
    public Map<String, String> createPayPalOrder(String userId, String userEmail, PayPalCreateRequest req) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("No hay ítems en la selección");
        }
        CartItem ci = cartItems.get(0);
        reservationRepository.findByUserIdAndStatus(userId, ReservationStatus.PENDING_PAYMENT)
            .forEach(reservationRepository::delete);

        assertAvailable(ci.getPropertyId(), ci.getCheckIn(), ci.getCheckOut());

        BigDecimal[] amounts = computeAmounts(ci.getPricePerNight(), ci.getNights());
        log.info("payment_provider_selected flow=paypal_create provider={}", paymentProvider.providerName());
        requirePayPalProvider("paypal_create");
        Map<String, String> paypal = paymentProvider.createOrder(amounts[2], req.getReturnUrl(), req.getCancelUrl());
        String paypalOrderId = paypal.get("paypalOrderId");

        Reservation r = Reservation.builder()
            .userId(userId)
            .userEmail(requireUserEmail(userEmail))
            .propertyId(ci.getPropertyId())
            .propertyName(ci.getPropertyName())
            .city(ci.getCity())
            .imageUrl(ci.getImageUrl())
            .checkIn(ci.getCheckIn())
            .checkOut(ci.getCheckOut())
            .nights(ci.getNights())
            .guests(ci.getGuests())
            .pricePerNight(ci.getPricePerNight())
            .subtotal(amounts[0])
            .taxAmount(amounts[1])
            .total(amounts[2])
            .status(ReservationStatus.PENDING_PAYMENT)
            .createdAt(LocalDateTime.now())
            .paymentId(paypalOrderId)
            .paypalOrderId(paypalOrderId)
            .build();

        r = reservationRepository.save(r);
        Map<String, String> out = new HashMap<>(paypal);
        out.put("localOrderId", String.valueOf(r.getId()));
        return out;
    }

    @Transactional
    public ReservationDto capturePayPalOrder(String userId, String userEmail, PayPalCaptureRequest req) {
        Reservation r = reservationRepository.findByPaypalOrderIdAndUserId(req.getPaypalOrderId(), userId)
            .orElseThrow(() -> new RuntimeException("No hay reserva PayPal pendiente para este usuario"));
        if (r.getStatus() != ReservationStatus.PENDING_PAYMENT) {
            throw new RuntimeException("La reserva no está pendiente de pago");
        }

        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Selección vacía; no se puede completar el pago");
        }

        log.info("payment_provider_selected flow=paypal_capture provider={}", paymentProvider.providerName());
        requirePayPalProvider("paypal_capture");
        String captureId = paymentProvider.captureOrder(req.getPaypalOrderId());

        r.setStatus(ReservationStatus.CONFIRMED);
        r.setUserEmail(requireUserEmail(userEmail));
        r.setPaymentId(captureId);
        r.setPaypalCaptureId(captureId);
        r.setPaypalCaptureStatus("COMPLETED");
        r.setTransactionRef(captureId);
        Reservation saved = reservationRepository.save(r);
        cartItemRepository.deleteByUserId(userId);

        publishConfirmedAfterCommit(saved, buildEvent(saved));
        return toDto(saved);
    }

    private ReservationDto finalizePaidReservation(String userId, String userEmail, CartItem ci, BigDecimal[] amounts, String paymentId) {
        reservationRepository.findByUserIdAndStatus(userId, ReservationStatus.PENDING_PAYMENT)
            .forEach(reservationRepository::delete);

        Reservation r = Reservation.builder()
            .userId(userId)
            .userEmail(userEmail)
            .propertyId(ci.getPropertyId())
            .propertyName(ci.getPropertyName())
            .city(ci.getCity())
            .imageUrl(ci.getImageUrl())
            .checkIn(ci.getCheckIn())
            .checkOut(ci.getCheckOut())
            .nights(ci.getNights())
            .guests(ci.getGuests())
            .pricePerNight(ci.getPricePerNight())
            .subtotal(amounts[0])
            .taxAmount(amounts[1])
            .total(amounts[2])
            .status(ReservationStatus.CONFIRMED)
            .createdAt(LocalDateTime.now())
            .paymentId(paymentId)
            .build();
        Reservation saved = reservationRepository.save(r);
        cartItemRepository.deleteByUserId(userId);

        publishConfirmedAfterCommit(saved, buildEvent(saved));
        return toDto(saved);
    }

    private void publishConfirmedAfterCommit(Reservation saved, OrderCompletedEvent event) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publishConfirmed(saved, event);
                }
            });
            return;
        }
        publishConfirmed(saved, event);
    }

    private void publishConfirmed(Reservation saved, OrderCompletedEvent event) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ORDER_CONFIRMED_ROUTING_KEY, event);
            log.info("event_published type=reservation.confirmed reservationId={} userId={} total={}",
                saved.getId(), saved.getUserId(), saved.getTotal());
        } catch (Exception ex) {
            log.error("event_publish_failed type=reservation.confirmed reservationId={} userId={} total={}",
                saved.getId(), saved.getUserId(), saved.getTotal(), ex);
        }
    }

    private OrderCompletedEvent buildEvent(Reservation r) {
        String num = "RES-" + r.getId();
        return OrderCompletedEvent.builder()
            .orderId(String.valueOf(r.getId()))
            .orderNumber(num)
            .userId(r.getUserId())
            .userEmail(r.getUserEmail())
            .total(r.getTotal())
            .createdAt(r.getCreatedAt())
            .propertyName(r.getPropertyName())
            .city(r.getCity())
            .checkIn(r.getCheckIn())
            .checkOut(r.getCheckOut())
            .nights(r.getNights())
            .items(List.of(OrderCompletedEvent.OrderItemInfo.builder()
                .bookId(r.getPropertyId())
                .bookTitle(r.getPropertyName() + " — " + r.getNights() + " noches")
                .quantity(r.getNights())
                .price(r.getPricePerNight())
                .build()))
            .build();
    }

    private void requirePayPalProvider(String flow) {
        String activeProvider = paymentProvider.providerName();
        if (!"paypal".equals(activeProvider)) {
            log.warn("paypal_provider_mismatch flow={} provider={}", flow, activeProvider);
            throw new PayPalApiException(
                "PAYPAL_PROVIDER_MISMATCH",
                "PayPal no está disponible porque el proveedor mock está activo.",
                null,
                null
            );
        }
    }

    private static String requireUserEmail(String userEmail) {
        if (userEmail == null || userEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Falta el correo del usuario (header X-User-Email o cuerpo).");
        }
        return userEmail.trim();
    }

    public boolean isAvailable(String propertyId, LocalDate checkIn, LocalDate checkOut) {
        return reservationRepository.countConflicting(propertyId, checkIn, checkOut) == 0;
    }

    public List<String> findOccupiedPropertyIds(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            return List.of();
        }
        return reservationRepository.findOccupiedPropertyIds(checkIn, checkOut);
    }

    @Transactional
    public ReservationDto updateReservation(String userId, Long reservationId, UpdateReservationRequest req) {
        Reservation r = reservationRepository.findByIdAndUserId(reservationId, userId)
            .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));
        if (r.getStatus() != ReservationStatus.PENDING_PAYMENT && r.getStatus() != ReservationStatus.CONFIRMED) {
            throw new RuntimeException("No se puede modificar esta reserva");
        }
        if (r.getStatus() == ReservationStatus.CONFIRMED && !r.getCheckIn().isAfter(LocalDate.now())) {
            throw new RuntimeException("No se puede modificar: la estadía ya comenzó o comienza hoy");
        }
        LocalDate in = req.getCheckIn();
        LocalDate out = req.getCheckOut();
        long nights = ChronoUnit.DAYS.between(in, out);
        if (nights <= 0) {
            throw new RuntimeException("La fecha de salida debe ser posterior al check-in");
        }
        long conflicts = reservationRepository.countConflictingExcluding(reservationId, r.getPropertyId(), in, out);
        if (conflicts > 0) {
            throw new RuntimeException("Las fechas seleccionadas no están disponibles para esta propiedad.");
        }
        BigDecimal[] amounts = computeAmounts(r.getPricePerNight(), (int) nights);
        r.setCheckIn(in);
        r.setCheckOut(out);
        r.setNights((int) nights);
        if (req.getGuests() != null) {
            r.setGuests(req.getGuests());
        }
        r.setSubtotal(amounts[0]);
        r.setTaxAmount(amounts[1]);
        r.setTotal(amounts[2]);
        return toDto(reservationRepository.save(r));
    }

    private void assertAvailable(String propertyId, LocalDate checkIn, LocalDate checkOut) {
        long c = reservationRepository.countConflicting(propertyId, checkIn, checkOut);
        if (c > 0) {
            throw new RuntimeException("Las fechas seleccionadas no están disponibles para esta propiedad.");
        }
    }

    private static BigDecimal[] computeAmounts(BigDecimal pricePerNight, int nights) {
        BigDecimal subtotal = pricePerNight.multiply(BigDecimal.valueOf(nights)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal tax = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(tax).setScale(2, RoundingMode.HALF_UP);
        return new BigDecimal[] { subtotal, tax, total };
    }

    public List<ReservationDto> getUserReservations(String userId) {
        return reservationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
            .map(this::toDto).collect(Collectors.toList());
    }

    public List<ReservationDto> getAllReservations() {
        return reservationRepository.findAllByOrderByCreatedAtDesc().stream()
            .map(this::toDto).collect(Collectors.toList());
    }

    public ReservationStatsDto getDashboardStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfToday = startOfToday.plusDays(1);

        long todayCreated = reservationRepository.countCreatedBetween(startOfToday, endOfToday);
        long pending = reservationRepository.countByStatus(ReservationStatus.PENDING_PAYMENT);
        long confirmedToday = reservationRepository.countByStatusAndCreatedAtBetween(
            ReservationStatus.CONFIRMED, startOfToday, endOfToday);
        long cancelledToday = reservationRepository.countByStatusAndCreatedAtBetween(
            ReservationStatus.CANCELLED, startOfToday, endOfToday);
        long completedToday = reservationRepository.countByStatusAndUpdatedAtBetween(
            ReservationStatus.COMPLETED, startOfToday, endOfToday);
        BigDecimal confirmedTotal = reservationRepository.sumConfirmedTotalBetween(startOfToday, endOfToday);
        if (confirmedTotal == null) {
            confirmedTotal = BigDecimal.ZERO;
        }

        List<ReservationStatsDto.DailyTotal> last7 = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = now.toLocalDate().minusDays(i);
            LocalDateTime s = day.atStartOfDay();
            LocalDateTime e = s.plusDays(1);
            BigDecimal sum = reservationRepository.sumConfirmedTotalBetween(s, e);
            last7.add(new ReservationStatsDto.DailyTotal(day.toString(), sum != null ? sum : BigDecimal.ZERO));
        }

        return ReservationStatsDto.builder()
            .reservationsTodayCount(todayCreated)
            .pendingPaymentCount(pending)
            .confirmedTodayCount(confirmedToday)
            .cancelledTodayCount(cancelledToday)
            .completedTodayCount(completedToday)
            .confirmedTodayTotal(confirmedTotal)
            .last7DaysConfirmed(last7)
            .build();
    }

    public ReservationDto getReservation(String userId, Long reservationId) {
        return reservationRepository.findByIdAndUserId(reservationId, userId)
            .map(this::toDto)
            .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));
    }

    @Transactional
    public ReservationDto cancelReservation(String userId, Long reservationId) {
        Reservation r = reservationRepository.findByIdAndUserId(reservationId, userId)
            .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));
        if (r.getStatus() != ReservationStatus.PENDING_PAYMENT && r.getStatus() != ReservationStatus.CONFIRMED) {
            throw new RuntimeException("Esta reserva no se puede cancelar");
        }
        if (r.getStatus() == ReservationStatus.CONFIRMED && !r.getCheckIn().isAfter(LocalDate.now())) {
            throw new RuntimeException("No se puede cancelar: la estadía ya comenzó o comienza hoy");
        }
        r.setStatus(ReservationStatus.CANCELLED);
        return toDto(reservationRepository.save(r));
    }

    private ReservationDto toDto(Reservation o) {
        return ReservationDto.builder()
            .id(o.getId())
            .reservationId(o.getId())
            .userId(o.getUserId())
            .userEmail(o.getUserEmail())
            .propertyId(o.getPropertyId())
            .propertyName(o.getPropertyName())
            .city(o.getCity())
            .country(o.getCountry())
            .imageUrl(o.getImageUrl())
            .checkIn(o.getCheckIn())
            .checkOut(o.getCheckOut())
            .nights(o.getNights())
            .guests(o.getGuests())
            .pricePerNight(o.getPricePerNight())
            .subtotal(o.getSubtotal())
            .taxAmount(o.getTaxAmount())
            .total(o.getTotal())
            .status(o.getStatus())
            .createdAt(o.getCreatedAt())
            .paymentId(o.getPaymentId())
            .paypalOrderId(o.getPaypalOrderId())
            .paypalCaptureId(o.getPaypalCaptureId())
            .paypalCaptureStatus(o.getPaypalCaptureStatus())
            .payerId(o.getPayerId())
            .transactionRef(o.getTransactionRef())
            .build();
    }
}
