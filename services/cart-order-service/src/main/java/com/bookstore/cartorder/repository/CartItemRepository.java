package com.bookstore.cartorder.repository;
import com.bookstore.cartorder.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUserId(String userId);
    Optional<CartItem> findByUserIdAndBookId(String userId, String bookId);
    void deleteByUserId(String userId);
}
