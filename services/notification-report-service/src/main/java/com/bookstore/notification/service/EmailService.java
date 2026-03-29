package com.bookstore.notification.service;
import com.bookstore.notification.event.OrderCompletedEvent;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service @RequiredArgsConstructor @Slf4j
public class EmailService {
    private final JavaMailSender mailSender;

    public void sendWelcomeEmail(String to, String username) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, false, "UTF-8");
            helper.setTo(to);
            helper.setFrom("noreply@bookstore.com");
            helper.setSubject("Welcome to BookStore!");
            helper.setText("<html><body><h1>Welcome to BookStore!</h1><p>Hello " + username + ", your account is ready.</p></body></html>", true);
            mailSender.send(msg);
            log.info("Welcome email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", to, e.getMessage());
        }
    }

    public void sendOrderConfirmationEmail(String to, String username, OrderCompletedEvent order, byte[] pdf) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setTo(to);
            helper.setFrom("noreply@bookstore.com");
            helper.setSubject("Order Confirmation - " + order.getOrderNumber());
            helper.setText(buildOrderHtml(username, order), true);
            helper.addAttachment("invoice-" + order.getOrderNumber() + ".pdf",
                () -> new java.io.ByteArrayInputStream(pdf), "application/pdf");
            mailSender.send(msg);
            log.info("Order confirmation email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send order confirmation email to {}: {}", to, e.getMessage());
        }
    }

    private String buildOrderHtml(String username, OrderCompletedEvent order) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body><h1>Order Confirmed!</h1>")
          .append("<p>Hello ").append(username).append(", your order has been confirmed.</p>")
          .append("<p><strong>Order #:</strong> ").append(order.getOrderNumber()).append("</p>");
        if (order.getItems() != null) {
            sb.append("<ul>");
            for (OrderCompletedEvent.OrderItemInfo item : order.getItems()) {
                sb.append("<li>").append(item.getBookTitle()).append(" x").append(item.getQuantity()).append("</li>");
            }
            sb.append("</ul>");
        }
        sb.append("<p><strong>Total: $").append(order.getTotal()).append("</strong></p></body></html>");
        return sb.toString();
    }
}
