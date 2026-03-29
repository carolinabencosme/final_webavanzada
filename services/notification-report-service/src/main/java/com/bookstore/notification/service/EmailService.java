package com.bookstore.notification.service;
import com.bookstore.notification.event.OrderCompletedEvent;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service @RequiredArgsConstructor @Slf4j
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    public void sendWelcomeEmail(String to, String username) {
        try {
            String loginUrl = frontendUrl.replaceAll("/$", "") + "/login";
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, false, "UTF-8");
            helper.setTo(to);
            helper.setFrom("noreply@bookstore.com");
            helper.setSubject("Welcome to BookStore — your access");
            String html = "<html><body style='font-family:system-ui,sans-serif'>"
                + "<h1>Welcome to BookStore</h1>"
                + "<p>Hello " + username + ", your account was created successfully.</p>"
                + "<p><strong>Sign-in email:</strong> " + to + "</p>"
                + "<p>Use the password you chose at registration to log in.</p>"
                + "<p><a href=\"" + loginUrl + "\">Open the bookstore</a></p>"
                + "<p style='color:#666;font-size:12px'>If the link does not work, copy this URL into your browser:<br/>" + loginUrl + "</p>"
                + "</body></html>";
            helper.setText(html, true);
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
