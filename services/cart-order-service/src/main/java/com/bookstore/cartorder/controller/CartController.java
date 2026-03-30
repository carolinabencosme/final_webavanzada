package com.bookstore.cartorder.controller;

import com.bookstore.cartorder.dto.*;
import com.bookstore.cartorder.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
    private static final String USER_ID_HEADER = "X-User-Id";

    private final CartService cartService;

    @GetMapping("/items")
    public ResponseEntity<ApiResponse<List<CartItemDto>>> getCart(@RequestHeader(USER_ID_HEADER) String userId) {
        return ResponseEntity.ok(ApiResponse.success("OK", cartService.getCart(userId)));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartItemDto>> add(@RequestHeader(USER_ID_HEADER) String userId,
                                                        @Valid @RequestBody AddToCartRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Added", cartService.addToCart(userId, req)));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartItemDto>> update(@RequestHeader(USER_ID_HEADER) String userId,
                                                           @PathVariable Long itemId,
                                                           @RequestParam int quantity) {
        return ResponseEntity.ok(ApiResponse.success("Updated", cartService.updateItem(userId, itemId, quantity)));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<Void>> remove(@RequestHeader(USER_ID_HEADER) String userId,
                                                    @PathVariable Long itemId) {
        cartService.removeItem(userId, itemId);
        return ResponseEntity.ok(ApiResponse.success("Removed", null));
    }

    @DeleteMapping("/items")
    public ResponseEntity<ApiResponse<Void>> clear(@RequestHeader(USER_ID_HEADER) String userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared", null));
    }
}
