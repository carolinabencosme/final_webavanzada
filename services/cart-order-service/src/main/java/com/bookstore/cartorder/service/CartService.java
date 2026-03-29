package com.bookstore.cartorder.service;
import com.bookstore.cartorder.client.CatalogClient;
import com.bookstore.cartorder.dto.*;
import com.bookstore.cartorder.entity.CartItem;
import com.bookstore.cartorder.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
@Service @RequiredArgsConstructor
public class CartService {
    private final CartItemRepository cartItemRepository;
    private final CatalogClient catalogClient;

    public List<CartItemDto> getCart(String userId) {
        return cartItemRepository.findByUserId(userId).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public CartItemDto addToCart(String userId, AddToCartRequest req) {
        ApiResponse<BookDto> resp = catalogClient.getBook(req.getBookId());
        BookDto book = resp.getData();
        CartItem item = cartItemRepository.findByUserIdAndBookId(userId, req.getBookId())
            .orElse(CartItem.builder().userId(userId).bookId(req.getBookId())
                .bookTitle(book.getTitle()).bookAuthor(book.getAuthor())
                .coverUrl(book.getCoverUrl()).price(book.getPrice()).quantity(0).build());
        item.setQuantity(item.getQuantity() + req.getQuantity());
        return toDto(cartItemRepository.save(item));
    }

    @Transactional
    public CartItemDto updateItem(String userId, Long itemId, int quantity) {
        CartItem item = cartItemRepository.findById(itemId)
            .filter(i -> i.getUserId().equals(userId))
            .orElseThrow(() -> new RuntimeException("Cart item not found"));
        if (quantity <= 0) { cartItemRepository.delete(item); return null; }
        item.setQuantity(quantity);
        return toDto(cartItemRepository.save(item));
    }

    @Transactional
    public void removeItem(String userId, Long itemId) {
        cartItemRepository.findById(itemId)
            .filter(i -> i.getUserId().equals(userId))
            .ifPresent(cartItemRepository::delete);
    }

    @Transactional
    public void clearCart(String userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    private CartItemDto toDto(CartItem i) {
        return CartItemDto.builder().id(i.getId()).bookId(i.getBookId())
            .bookTitle(i.getBookTitle()).bookAuthor(i.getBookAuthor())
            .coverUrl(i.getCoverUrl()).quantity(i.getQuantity()).price(i.getPrice()).build();
    }
}
