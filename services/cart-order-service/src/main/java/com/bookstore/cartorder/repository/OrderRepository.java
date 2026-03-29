package com.bookstore.cartorder.repository;
import com.bookstore.cartorder.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);
}
