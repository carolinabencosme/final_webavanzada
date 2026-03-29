package com.bookstore.notification.document;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
@Document(collection="notification_logs")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationLog {
    @Id private String id;
    private String type;
    private String userId,email,subject,status,orderId,errorMessage;
    private LocalDateTime createdAt;
}
