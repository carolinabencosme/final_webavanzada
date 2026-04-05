package com.hospedaje.notification.event;
import lombok.*;
import java.time.LocalDateTime;
@Data @NoArgsConstructor @AllArgsConstructor
public class UserRegisteredEvent {
    private String userId,email,username;
    private LocalDateTime timestamp;
}
