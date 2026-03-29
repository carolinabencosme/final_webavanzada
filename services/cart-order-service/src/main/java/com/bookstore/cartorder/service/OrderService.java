package com.bookstore.cartorder.service;

import com.bookstore.cartorder.config.RabbitMQConfig;
import com.bookstore.cartorder.dto.*;
import com.bookstore.cartorder.entity.*;
import com.bookstore.cartorder.event.OrderCompletedEvent;
import com.bookstore.cartorder.payment.PayPalClient;
import com.bookstore.cartorder.payment.PaymentProvider;
import com.bookstore.cartorder.repository.CartItemRepository;
import com.bookstore.cartorder.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final PaymentProvider paymentProvider;
    private final PayPalClient payPalClient;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public OrderDto checkout(String userId, CheckoutRequest req) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        if (cartItems.isEmpty()) throw new RuntimeException("Cart is empty");
        BigDecimal total = cartTotal(cartItems);
        String paymentId = paymentProvider.charge(req.getUserEmail(), total, req.getCardNumber(), req.getCardExpiry(), req.getCardCvc());
        return finalizePaidOrder(userId, req.getUserEmail(), cartItems, total, paymentId);
    }

    /** Create PayPal order + local PENDING order; cart unchanged until capture. */
    @Transactional
    public Map<String, String> createPayPalOrder(String userId, PayPalCreateRequest req) {
        if (!payPalClient.isConfigured()) {
            throw new IllegalStateException("PayPal is not configured (set PAYPAL_CLIENT_ID, PAYPAL_CLIENT_SECRET, paypal.enabled=true)");
        }
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        if (cartItems.isEmpty()) throw new RuntimeException("Cart is empty");
        orderRepository.findByUserIdAndStatus(userId, OrderStatus.PENDING).forEach(orderRepository::delete);

        BigDecimal total = cartTotal(cartItems);
        Order order = Order.builder()
            .userId(userId)
            .userEmail(req.getUserEmail())
            .status(OrderStatus.PENDING)
            .total(total)
            .createdAt(LocalDateTime.now())
            .paymentId("pending")
            .build();
        List<OrderItem> orderItems = buildOrderItems(order, cartItems);
        order.setItems(orderItems);
        order = orderRepository.save(order);

        Map<String, String> paypal = payPalClient.createOrder(total, req.getReturnUrl(), req.getCancelUrl());
        String paypalOrderId = paypal.get("paypalOrderId");
        order.setPaymentId(paypalOrderId);
        orderRepository.save(order);

        Map<String, String> out = new HashMap<>(paypal);
        out.put("localOrderId", String.valueOf(order.getId()));
        return out;
    }

    @Transactional
    public OrderDto capturePayPalOrder(String userId, PayPalCaptureRequest req) {
        if (!payPalClient.isConfigured()) {
            throw new IllegalStateException("PayPal is not configured");
        }
        Order order = orderRepository.findByPaymentIdAndUserId(req.getPaypalOrderId(), userId)
            .orElseThrow(() -> new RuntimeException("No pending PayPal order for this user"));
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Order is not pending payment");
        }

        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty; cannot complete order");
        }

        String captureId = payPalClient.captureOrder(req.getPaypalOrderId());
        return finalizePaidOrder(userId, req.getUserEmail(), cartItems, order.getTotal(), captureId);
    }

    private OrderDto finalizePaidOrder(String userId, String userEmail, List<CartItem> cartItems, BigDecimal total, String paymentId) {
        orderRepository.findByUserIdAndStatus(userId, OrderStatus.PENDING).forEach(orderRepository::delete);

        Order order = Order.builder().userId(userId).userEmail(userEmail)
            .status(OrderStatus.PAID).total(total).createdAt(LocalDateTime.now()).paymentId(paymentId).build();
        List<OrderItem> orderItems = buildOrderItems(order, cartItems);
        order.setItems(orderItems);
        Order saved = orderRepository.save(order);
        cartItemRepository.deleteByUserId(userId);

        String ordNum = "ORD-" + saved.getId();
        OrderCompletedEvent event = OrderCompletedEvent.builder()
            .orderId(String.valueOf(saved.getId()))
            .orderNumber(ordNum)
            .userId(userId)
            .userEmail(userEmail)
            .total(total)
            .items(orderItems.stream().map(oi -> OrderCompletedEvent.OrderItemInfo.builder()
                .bookId(oi.getBookId()).bookTitle(oi.getBookTitle())
                .quantity(oi.getQuantity()).price(oi.getPrice()).build()
            ).collect(Collectors.toList())).build();
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ORDER_COMPLETED_ROUTING_KEY, event);
        return toDto(saved);
    }

    private static BigDecimal cartTotal(List<CartItem> cartItems) {
        return cartItems.stream()
            .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static List<OrderItem> buildOrderItems(Order order, List<CartItem> cartItems) {
        return cartItems.stream().map(ci ->
            OrderItem.builder().order(order).bookId(ci.getBookId()).bookTitle(ci.getBookTitle())
                .bookAuthor(ci.getBookAuthor()).coverUrl(ci.getCoverUrl())
                .quantity(ci.getQuantity()).price(ci.getPrice()).build()
        ).collect(Collectors.toList());
    }

    public List<OrderDto> getUserOrders(String userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<OrderDto> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toDto).collect(Collectors.toList());
    }

    public OrderStatsDto getDashboardStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfToday = startOfToday.plusDays(1);

        long pending = orderRepository.countByStatus(OrderStatus.PENDING);
        long paidToday = orderRepository.countByStatusAndCreatedAtBetween(OrderStatus.PAID, startOfToday, endOfToday);
        BigDecimal paidTodayTotal = orderRepository.sumPaidTotalBetween(startOfToday, endOfToday);
        if (paidTodayTotal == null) paidTodayTotal = BigDecimal.ZERO;

        List<OrderStatsDto.DailyTotal> last7 = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = now.toLocalDate().minusDays(i);
            LocalDateTime s = day.atStartOfDay();
            LocalDateTime e = s.plusDays(1);
            BigDecimal sum = orderRepository.sumPaidTotalBetween(s, e);
            last7.add(new OrderStatsDto.DailyTotal(day.toString(), sum != null ? sum : BigDecimal.ZERO));
        }

        return OrderStatsDto.builder()
            .pendingCount(pending)
            .paidTodayCount(paidToday)
            .paidTodayTotal(paidTodayTotal)
            .last7DaysPaid(last7)
            .build();
    }

    public OrderDto getOrder(String userId, Long orderId) {
        return orderRepository.findById(orderId)
            .filter(o -> o.getUserId().equals(userId))
            .map(this::toDto)
            .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    private OrderDto toDto(Order o) {
        List<CartItemDto> items = o.getItems() == null ? List.of() : o.getItems().stream().map(oi ->
            CartItemDto.builder().id(oi.getId()).bookId(oi.getBookId()).bookTitle(oi.getBookTitle())
                .bookAuthor(oi.getBookAuthor()).coverUrl(oi.getCoverUrl())
                .quantity(oi.getQuantity()).price(oi.getPrice()).build()
        ).collect(Collectors.toList());
        return OrderDto.builder().id(o.getId()).userId(o.getUserId()).userEmail(o.getUserEmail())
            .paymentId(o.getPaymentId()).status(o.getStatus()).total(o.getTotal())
            .createdAt(o.getCreatedAt()).items(items).build();
    }
}
