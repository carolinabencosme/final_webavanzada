package com.bookstore.notification.controller;
import com.bookstore.notification.document.NotificationLog;
import com.bookstore.notification.dto.ApiResponse;
import com.bookstore.notification.service.NotificationService;
import com.bookstore.notification.service.PdfService;
import com.bookstore.notification.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/notifications") @RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    private final PdfService pdfService;

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<List<NotificationLog>>> getUserNotifications(@PathVariable String userId) {
        return ResponseEntity.ok(ApiResponse.success("OK", notificationService.getUserNotifications(userId)));
    }

    @PostMapping("/invoice")
    public ResponseEntity<byte[]> generateInvoice(@RequestBody OrderCreatedEvent event) {
        byte[] pdf = pdfService.generateInvoice(event);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice-" + event.getOrderId() + ".pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf);
    }
}
