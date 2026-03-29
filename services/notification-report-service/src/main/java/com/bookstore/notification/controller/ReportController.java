package com.bookstore.notification.controller;
import com.bookstore.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/reports") @RequiredArgsConstructor
public class ReportController {
    private final NotificationService notificationService;
    @GetMapping("/invoice/{orderId}")
    public ResponseEntity<byte[]> getInvoice(@PathVariable String orderId) {
        byte[] pdf = notificationService.getOrGenerateInvoice(orderId);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=invoice-"+orderId+".pdf")
            .body(pdf);
    }
}
