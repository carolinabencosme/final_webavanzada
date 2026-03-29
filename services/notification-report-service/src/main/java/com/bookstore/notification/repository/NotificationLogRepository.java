package com.bookstore.notification.repository;
import com.bookstore.notification.document.NotificationLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;
public interface NotificationLogRepository extends MongoRepository<NotificationLog,String> {
    List<NotificationLog> findTop50ByOrderByCreatedAtDesc();
    Optional<NotificationLog> findByOrderId(String orderId);
}
