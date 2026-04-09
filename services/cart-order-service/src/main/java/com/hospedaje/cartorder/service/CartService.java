package com.hospedaje.cartorder.service;

import com.hospedaje.cartorder.client.CatalogClient;
import com.hospedaje.cartorder.dto.ApiResponse;
import com.hospedaje.cartorder.dto.AddToCartRequest;
import com.hospedaje.cartorder.dto.CartItemDto;
import com.hospedaje.cartorder.dto.PropertyDto;
import com.hospedaje.cartorder.entity.CartItem;
import com.hospedaje.cartorder.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final CatalogClient catalogClient;
    private final RoomAvailabilityService roomAvailabilityService;

    public List<CartItemDto> getCart(String userId) {
        return cartItemRepository.findByUserId(userId).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public CartItemDto addToCart(String userId, AddToCartRequest req) {
        ApiResponse<PropertyDto> resp = catalogClient.getProperty(req.getPropertyId());
        PropertyDto p = resp.getData();
        if (p == null) {
            throw new RuntimeException("Propiedad no encontrada");
        }
        if (req.getGuests() > p.getMaxGuests()) {
            throw new RuntimeException("El número de huéspedes supera el máximo permitido (" + p.getMaxGuests() + ")");
        }

        int nights = (int) ChronoUnit.DAYS.between(req.getCheckIn(), req.getCheckOut());
        if (nights <= 0) {
            throw new RuntimeException("Rango de fechas inválido");
        }

        String roomType = roomAvailabilityService.normalizeRoomType(p.getRoomType());
        if (!roomAvailabilityService.isRoomTypeAvailable(req.getPropertyId(), roomType, req.getCheckIn(), req.getCheckOut())) {
            throw new RuntimeException("Las fechas no están disponibles para esta propiedad");
        }

        BigDecimal pricePerNight = p.getPricePerNight() != null ? p.getPricePerNight() : BigDecimal.ZERO;
        BigDecimal lineTotal = pricePerNight.multiply(BigDecimal.valueOf(nights)).setScale(2, RoundingMode.HALF_UP);

        cartItemRepository.deleteByUserId(userId);

        CartItem item = CartItem.builder()
            .userId(userId)
            .propertyId(req.getPropertyId())
            .propertyName(p.getName())
            .roomType(roomType)
            .city(p.getCity())
            .imageUrl(p.getImageUrl())
            .checkIn(req.getCheckIn())
            .checkOut(req.getCheckOut())
            .quantity(1)
            .guests(req.getGuests())
            .nights(nights)
            .pricePerNight(pricePerNight)
            .lineTotal(lineTotal)
            .build();

        return toDto(cartItemRepository.save(item));
    }

    @Transactional
    public CartItemDto updateItem(String userId, Long itemId, int quantity) {
        throw new UnsupportedOperationException("Actualice fechas volviendo a agregar la propiedad al itinerario.");
    }

    @Transactional
    public void removeItem(String userId, Long itemId) {
        CartItem item = cartItemRepository.findByIdAndUserId(itemId, userId)
            .orElseThrow(() -> new RuntimeException("Ítem no encontrado"));
        cartItemRepository.delete(item);
    }

    @Transactional
    public void clearCart(String userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    private CartItemDto toDto(CartItem i) {
        int nights = 0;
        if (i.getNights() != null && i.getNights() > 0) {
            nights = i.getNights();
        } else if (i.getCheckIn() != null && i.getCheckOut() != null) {
            nights = (int) ChronoUnit.DAYS.between(i.getCheckIn(), i.getCheckOut());
        }
        int guests = (i.getGuests() != null && i.getGuests() > 0) ? i.getGuests() : 1;

        return CartItemDto.builder()
            .id(i.getId())
            .propertyId(i.getPropertyId())
            .propertyName(i.getPropertyName())
            .roomType(i.getRoomType())
            .city(i.getCity())
            .imageUrl(i.getImageUrl())
            .checkIn(i.getCheckIn())
            .checkOut(i.getCheckOut())
            .quantity((i.getQuantity() != null && i.getQuantity() > 0) ? i.getQuantity() : 1)
            .guests(guests)
            .nights(nights)
            .pricePerNight(i.getPricePerNight())
            .lineTotal(i.getLineTotal())
            .build();
    }
}
