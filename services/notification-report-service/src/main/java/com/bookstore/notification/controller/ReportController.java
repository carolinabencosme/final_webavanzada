package com.bookstore.notification.controller;
import com.bookstore.notification.service.NotificationService;
import com.bookstore.notification.service.InvoiceAccessTokenService;
import com.bookstore.notification.service.InvoiceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/reports") @RequiredArgsConstructor
public class ReportController {
    private final NotificationService notificationService;
    private final InvoiceAccessTokenService invoiceAccessTokenService;

    @GetMapping("/invoice/{orderId}")
    public ResponseEntity<byte[]> getInvoice(@PathVariable String orderId) {
        byte[] pdf = notificationService.getOrGenerateInvoice(orderId);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=invoice-"+orderId+".pdf")
            .body(pdf);
    }

    @GetMapping("/invoice/public/{orderId}")
    public ResponseEntity<byte[]> getPublicInvoice(@PathVariable String orderId, @RequestParam("token") String token) {
        if (!invoiceAccessTokenService.isValid(orderId, token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        byte[] pdf = notificationService.getOrGenerateInvoice(orderId);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=invoice-"+orderId+".pdf")
            .body(pdf);
    }

    @ExceptionHandler(InvoiceNotFoundException.class)
    public ResponseEntity<String> handleInvoiceNotFound(InvoiceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}
