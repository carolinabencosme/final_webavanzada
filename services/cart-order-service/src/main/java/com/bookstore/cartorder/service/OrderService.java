package com.bookstore.cartorder.service;
import com.bookstore.cartorder.config.RabbitMQConfig;
import com.bookstore.cartorder.dto.*;
import com.bookstore.cartorder.entity.*;
import com.bookstore.cartorder.event.OrderCreatedEvent;
import com.bookstore.cartorder.payment.PaymentProvider;
import com.bookstore.cartorder.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
@Service @RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final PaymentProvider paymentProvider;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public OrderDto checkout(String userId, CheckoutRequest req) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        if (cartItems.isEmpty()) throw new RuntimeException("Cart is empty");
        BigDecimal total = cartItems.stream()
            .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        String paymentId = paymentProvider.charge(req.getUserEmail(), total, req.getCardNumber(), req.getCardExpiry(), req.getCardCvc());
        Order order = Order.builder().userId(userId).userEmail(req.getUserEmail())
            .status(OrderStatus.PAID).total(total).createdAt(LocalDateTime.now()).paymentId(paymentId).build();
        List<OrderItem> orderItems = cartItems.stream().map(ci ->
            OrderItem.builder().order(order).bookId(ci.getBookId()).bookTitle(ci.getBookTitle())
                .bookAuthor(ci.getBookAuthor()).coverUrl(ci.getCoverUrl())
                .quantity(ci.getQuantity()).price(ci.getPrice()).build()
        ).collect(Collectors.toList());
        order.setItems(orderItems);
        Order saved = orderRepository.save(order);
        cartItemRepository.deleteByUserId(userId);
        OrderCreatedEvent event = OrderCreatedEvent.builder()
            .orderId(saved.getId()).userId(userId).userEmail(req.getUserEmail()).total(total)
            .items(orderItems.stream().map(oi -> OrderCreatedEvent.OrderItemEvent.builder()
                .bookId(oi.getBookId()).bookTitle(oi.getBookTitle())
                .quantity(oi.getQuantity()).price(oi.getPrice()).build()
            ).collect(Collectors.toList())).build();
        rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_EXCHANGE, RabbitMQConfig.ORDER_CREATED_ROUTING_KEY, event);
        return toDto(saved);
    }

    public List<OrderDto> getUserOrders(String userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(this::toDto).collect(Collectors.toList());
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
