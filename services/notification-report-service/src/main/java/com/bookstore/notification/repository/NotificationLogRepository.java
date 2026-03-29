package com.bookstore.notification.repository;
import com.bookstore.notification.document.NotificationLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
public interface NotificationLogRepository extends MongoRepository<NotificationLog, String> {
    List<NotificationLog> findByUserIdOrderByCreatedAtDesc(String userId);
    List<NotificationLog> findByOrderId(Long orderId);
}
