package com.bookstore.cartorder.controller;

import com.bookstore.cartorder.dto.*;
import com.bookstore.cartorder.service.CartService;
import com.bookstore.cartorder.web.RequestIdentityResolver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;
    private final RequestIdentityResolver identityResolver;

    @GetMapping("/items")
    public ResponseEntity<ApiResponse<List<CartItemDto>>> getCart(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success("OK", cartService.getCart(identityResolver.resolveUserId(request))));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartItemDto>> add(HttpServletRequest request,
                                                        @Valid @RequestBody AddToCartRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Added", cartService.addToCart(identityResolver.resolveUserId(request), req)));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartItemDto>> update(HttpServletRequest request,
                                                           @PathVariable Long itemId,
                                                           @RequestParam int quantity) {
        return ResponseEntity.ok(ApiResponse.success("Updated", cartService.updateItem(identityResolver.resolveUserId(request), itemId, quantity)));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<Void>> remove(HttpServletRequest request,
                                                    @PathVariable Long itemId) {
        cartService.removeItem(identityResolver.resolveUserId(request), itemId);
        return ResponseEntity.ok(ApiResponse.success("Removed", null));
    }

    @DeleteMapping("/items")
    public ResponseEntity<ApiResponse<Void>> clear(HttpServletRequest request) {
        cartService.clearCart(identityResolver.resolveUserId(request));
        return ResponseEntity.ok(ApiResponse.success("Cart cleared", null));
    }
}
