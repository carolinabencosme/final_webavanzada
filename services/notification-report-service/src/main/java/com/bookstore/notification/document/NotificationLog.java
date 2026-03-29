package com.bookstore.notification.document;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
@Document(collection = "notification_logs")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationLog {
    @Id private String id;
    private String userId;
    private String userEmail;
    private String type;
    private String subject;
    private String body;
    private Long orderId;
    private String status;
    private LocalDateTime createdAt;
}
