package com.hospedaje.notification.document;

import com.hospedaje.notification.event.OrderCompletedEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Document("invoice_snapshots")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceSnapshot {
    @Id
    private String id;

    @Indexed(unique = true)
    private String orderId;
    private String orderNumber;
    private String userId;
    private String userEmail;
    private BigDecimal total;
    private LocalDateTime createdAt;
    private List<OrderCompletedEvent.OrderItemInfo> items;
}
