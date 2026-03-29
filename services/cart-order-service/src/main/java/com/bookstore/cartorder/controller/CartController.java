package com.bookstore.cartorder.controller;
import com.bookstore.cartorder.dto.*;
import com.bookstore.cartorder.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/cart") @RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<List<CartItemDto>>> getCart(@PathVariable String userId) {
        return ResponseEntity.ok(ApiResponse.success("OK", cartService.getCart(userId)));
    }

    @PostMapping("/{userId}")
    public ResponseEntity<ApiResponse<CartItemDto>> add(@PathVariable String userId, @Valid @RequestBody AddToCartRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Added", cartService.addToCart(userId, req)));
    }

    @PutMapping("/{userId}/items/{itemId}")
    public ResponseEntity<ApiResponse<CartItemDto>> update(@PathVariable String userId, @PathVariable Long itemId, @RequestParam int quantity) {
        return ResponseEntity.ok(ApiResponse.success("Updated", cartService.updateItem(userId, itemId, quantity)));
    }

    @DeleteMapping("/{userId}/items/{itemId}")
    public ResponseEntity<ApiResponse<Void>> remove(@PathVariable String userId, @PathVariable Long itemId) {
        cartService.removeItem(userId, itemId);
        return ResponseEntity.ok(ApiResponse.success("Removed", null));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> clear(@PathVariable String userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared", null));
    }
}
