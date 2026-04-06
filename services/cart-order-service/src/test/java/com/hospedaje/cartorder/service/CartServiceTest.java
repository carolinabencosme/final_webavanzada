package com.hospedaje.cartorder.service;

import com.hospedaje.cartorder.client.CatalogClient;
import com.hospedaje.cartorder.dto.AddToCartRequest;
import com.hospedaje.cartorder.dto.ApiResponse;
import com.hospedaje.cartorder.dto.CartItemDto;
import com.hospedaje.cartorder.dto.PropertyDto;
import com.hospedaje.cartorder.entity.CartItem;
import com.hospedaje.cartorder.repository.CartItemRepository;
import com.hospedaje.cartorder.repository.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private CatalogClient catalogClient;
    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private CartService cartService;

    @Test
    void addPropertyToCartShouldPersistQuantityOne() {
        String userId = "user-123";
        AddToCartRequest request = new AddToCartRequest(
            "69d2d798d6da840eda96f329",
            LocalDate.of(2026, 4, 7),
            LocalDate.of(2026, 4, 9),
            2,
            null
        );

        PropertyDto property = PropertyDto.builder()
            .id("69d2d798d6da840eda96f329")
            .name("Casa Centro")
            .city("Madrid")
            .imageUrl("img")
            .maxGuests(4)
            .pricePerNight(new BigDecimal("100.00"))
            .build();

        when(catalogClient.getProperty(request.getPropertyId())).thenReturn(ApiResponse.success("ok", property));
        when(reservationRepository.countConflicting(request.getPropertyId(), request.getCheckIn(), request.getCheckOut())).thenReturn(0L);
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> {
            CartItem entity = invocation.getArgument(0);
            entity.setId(10L);
            return entity;
        });

        CartItemDto result = cartService.addToCart(userId, request);

        ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository).save(captor.capture());
        CartItem persisted = captor.getValue();

        assertThat(persisted.getQuantity()).isEqualTo(1);
        assertThat(result.getQuantity()).isEqualTo(1);
        assertThat(result.getNights()).isEqualTo(2);
        assertThat(result.getLineTotal()).isEqualByComparingTo("200.00");
        verify(cartItemRepository).deleteByUserId(eq(userId));
    }
}
