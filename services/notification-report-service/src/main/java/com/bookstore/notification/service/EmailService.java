package com.bookstore.notification.service;
import com.bookstore.notification.event.OrderCompletedEvent;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
            helper.setSubject("Welcome to BookStore — your account is ready");
            String safeName = esc(username);
            String safeEmail = esc(to);
            String safeLoginUrl = esc(loginUrl);
            String brand = "BookStore";

            String html =
                "<!doctype html><html><head><meta charset='UTF-8'/>" +
                    "<meta name='viewport' content='width=device-width, initial-scale=1.0'/>" +
                    "<title>Welcome</title></head>" +
                    "<body style='margin:0;padding:0;background:#f6f0e8;color:#121820;font-family:system-ui,-apple-system,Segoe UI,Roboto,Helvetica,Arial,sans-serif;'>" +
                    "<table role='presentation' width='100%' cellspacing='0' cellpadding='0' style='background:#f6f0e8;padding:24px 0;'>" +
                      "<tr><td align='center'>" +
                        "<table role='presentation' width='600' cellspacing='0' cellpadding='0' style='width:600px;max-width:92vw;background:#ffffff;border:1px solid rgba(18,24,32,0.10);'>" +
                          "<tr><td style='padding:22px 24px;background:#121820;color:#f6f0e8;'>" +
                            "<div style='font-weight:700;letter-spacing:0.06em;text-transform:uppercase;font-size:12px;color:rgba(246,240,232,0.8)'>PUCMM</div>" +
                            "<div style='font-family:Georgia,Times,serif;font-size:28px;line-height:1.15;font-weight:700;margin-top:6px;'>" + brand + "</div>" +
                            "<div style='margin-top:8px;color:rgba(246,240,232,0.75);font-size:13px;line-height:1.45'>Your access details and sign-in link.</div>" +
                          "</td></tr>" +
                          "<tr><td style='padding:26px 24px 10px 24px;'>" +
                            "<h1 style='margin:0 0 10px 0;font-size:22px;line-height:1.25;'>Welcome, " + safeName + "</h1>" +
                            "<p style='margin:0 0 14px 0;color:rgba(18,24,32,0.78);font-size:14px;line-height:1.6;'>Your account was created successfully. You can now browse the catalog, save items, and place orders.</p>" +
                            "<div style='background:#f7f5f2;border:1px solid rgba(18,24,32,0.10);padding:14px 14px;margin:16px 0;'>" +
                              "<div style='font-size:12px;letter-spacing:0.06em;text-transform:uppercase;color:rgba(18,24,32,0.60);margin-bottom:6px;'>Sign-in</div>" +
                              "<div style='font-size:14px;'><strong>Email:</strong> " + safeEmail + "</div>" +
                              "<div style='font-size:13px;color:rgba(18,24,32,0.70);margin-top:6px;'>Use the password you chose during registration.</div>" +
                            "</div>" +
                            "<div style='padding:8px 0 18px 0;'>" +
                              "<a href='" + safeLoginUrl + "' style='display:inline-block;background:#4f46e5;color:#ffffff;text-decoration:none;padding:12px 16px;font-weight:700;font-size:13px;letter-spacing:0.02em;'>Open BookStore</a>" +
                            "</div>" +
                            "<p style='margin:0 0 10px 0;color:rgba(18,24,32,0.60);font-size:12px;line-height:1.55;'>If the button doesn't work, copy and paste this URL:</p>" +
                            "<p style='margin:0 0 18px 0;font-family:ui-monospace,SFMono-Regular,Menlo,Monaco,Consolas,monospace;font-size:12px;color:#121820;word-break:break-all;'>" + safeLoginUrl + "</p>" +
                          "</td></tr>" +
                          "<tr><td style='padding:18px 24px;background:#fbfaf8;border-top:1px solid rgba(18,24,32,0.08);'>" +
                            "<div style='font-size:11px;line-height:1.55;color:rgba(18,24,32,0.55);'>" +
                              "This is a transactional message sent by " + brand + ". If you did not create this account, you can ignore this email." +
                            "</div>" +
                          "</td></tr>" +
                        "</table>" +
                      "</td></tr>" +
                    "</table>" +
                    "</body></html>";
            helper.setText(html, true);
            mailSender.send(msg);
            log.info("Welcome email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", to, e.getMessage());
        }
    }

    public void sendOrderConfirmationEmail(String to, String username, OrderCompletedEvent order, byte[] pdf, String invoiceToken) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setTo(to);
            helper.setFrom("noreply@bookstore.com");
            helper.setSubject("Order confirmed — " + order.getOrderNumber());
            helper.setText(buildOrderHtml(username, order, invoiceToken), true);
            helper.addAttachment("invoice-" + order.getOrderNumber() + ".pdf",
                () -> new java.io.ByteArrayInputStream(pdf), "application/pdf");
            mailSender.send(msg);
            log.info("Order confirmation email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send order confirmation email to {}: {}", to, e.getMessage());
        }
    }

    private String buildOrderHtml(String username, OrderCompletedEvent order, String invoiceToken) {
        String base = frontendUrl == null ? "http://localhost:3000" : frontendUrl.replaceAll("/$", "");
        String ordersUrl = esc(base + "/my-orders");
        String invoiceUrl = esc("http://localhost:8080/api/reports/invoice/public/" +
            (order.getOrderId() != null ? order.getOrderId() : "") +
            "?token=" + (invoiceToken != null ? invoiceToken : ""));

        String safeName = esc(username);
        String safeOrderNo = esc(order.getOrderNumber());

        BigDecimal total = order.getTotal() != null ? order.getTotal() : BigDecimal.ZERO;
        BigDecimal tax = total.multiply(BigDecimal.valueOf(0.1)).divide(BigDecimal.valueOf(1.1), 2, RoundingMode.HALF_UP);
        BigDecimal subtotal = total.subtract(tax);

        StringBuilder rows = new StringBuilder();
        if (order.getItems() != null) {
            for (OrderCompletedEvent.OrderItemInfo item : order.getItems()) {
                String title = esc(item.getBookTitle());
                int qty = item.getQuantity();
                BigDecimal price = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
                BigDecimal line = price.multiply(BigDecimal.valueOf(qty));
                rows.append("<tr>")
                    .append("<td style='padding:10px 8px;border-bottom:1px solid rgba(18,24,32,0.08);'>")
                    .append("<div style='font-size:13px;font-weight:600;color:#121820;'>").append(title).append("</div>")
                    .append("</td>")
                    .append("<td style='padding:10px 8px;border-bottom:1px solid rgba(18,24,32,0.08);text-align:center;font-size:13px;color:rgba(18,24,32,0.75);'>").append(qty).append("</td>")
                    .append("<td style='padding:10px 8px;border-bottom:1px solid rgba(18,24,32,0.08);text-align:right;font-size:13px;color:rgba(18,24,32,0.75);'>$").append(money(price)).append("</td>")
                    .append("<td style='padding:10px 8px;border-bottom:1px solid rgba(18,24,32,0.08);text-align:right;font-size:13px;font-weight:600;color:#121820;'>$").append(money(line)).append("</td>")
                    .append("</tr>");
            }
        }
        if (rows.length() == 0) {
            rows.append("<tr><td colspan='4' style='padding:14px 8px;color:rgba(18,24,32,0.65);font-size:13px;border-bottom:1px solid rgba(18,24,32,0.08);'>(No line items)</td></tr>");
        }

        String brand = "BookStore";
        return "<!doctype html><html><head><meta charset='UTF-8'/>" +
            "<meta name='viewport' content='width=device-width, initial-scale=1.0'/>" +
            "<title>Order confirmed</title></head>" +
            "<body style='margin:0;padding:0;background:#f6f0e8;color:#121820;font-family:system-ui,-apple-system,Segoe UI,Roboto,Helvetica,Arial,sans-serif;'>" +
            "<table role='presentation' width='100%' cellspacing='0' cellpadding='0' style='background:#f6f0e8;padding:24px 0;'>" +
              "<tr><td align='center'>" +
                "<table role='presentation' width='600' cellspacing='0' cellpadding='0' style='width:600px;max-width:92vw;background:#ffffff;border:1px solid rgba(18,24,32,0.10);'>" +
                  "<tr><td style='padding:22px 24px;background:#121820;color:#f6f0e8;'>" +
                    "<div style='font-weight:700;letter-spacing:0.06em;text-transform:uppercase;font-size:12px;color:rgba(246,240,232,0.8)'>Receipt</div>" +
                    "<div style='font-family:Georgia,Times,serif;font-size:26px;line-height:1.15;font-weight:700;margin-top:6px;'>" + brand + "</div>" +
                    "<div style='margin-top:8px;color:rgba(246,240,232,0.75);font-size:13px;line-height:1.45'>Order confirmed. Invoice attached as PDF.</div>" +
                  "</td></tr>" +
                  "<tr><td style='padding:26px 24px 12px 24px;'>" +
                    "<h1 style='margin:0 0 10px 0;font-size:20px;line-height:1.25;'>Thank you, " + safeName + "</h1>" +
                    "<p style='margin:0 0 14px 0;color:rgba(18,24,32,0.78);font-size:14px;line-height:1.6;'>We've received your payment and your order is now confirmed.</p>" +
                    "<div style='background:#f7f5f2;border:1px solid rgba(18,24,32,0.10);padding:12px 14px;margin:16px 0;'>" +
                      "<div style='font-size:12px;letter-spacing:0.06em;text-transform:uppercase;color:rgba(18,24,32,0.60);margin-bottom:6px;'>Order</div>" +
                      "<div style='font-size:14px;'><strong>Order #:</strong> " + safeOrderNo + "</div>" +
                      "<div style='font-size:12px;color:rgba(18,24,32,0.65);margin-top:6px;'>Keep this number for reference.</div>" +
                    "</div>" +
                    "<table role='presentation' width='100%' cellspacing='0' cellpadding='0' style='border-collapse:collapse;border:1px solid rgba(18,24,32,0.10);'>" +
                      "<thead>" +
                        "<tr style='background:#fbfaf8;'>" +
                          "<th align='left' style='padding:10px 8px;font-size:12px;letter-spacing:0.06em;text-transform:uppercase;color:rgba(18,24,32,0.65);border-bottom:1px solid rgba(18,24,32,0.08);'>Title</th>" +
                          "<th align='center' style='padding:10px 8px;font-size:12px;letter-spacing:0.06em;text-transform:uppercase;color:rgba(18,24,32,0.65);border-bottom:1px solid rgba(18,24,32,0.08);'>Qty</th>" +
                          "<th align='right' style='padding:10px 8px;font-size:12px;letter-spacing:0.06em;text-transform:uppercase;color:rgba(18,24,32,0.65);border-bottom:1px solid rgba(18,24,32,0.08);'>Price</th>" +
                          "<th align='right' style='padding:10px 8px;font-size:12px;letter-spacing:0.06em;text-transform:uppercase;color:rgba(18,24,32,0.65);border-bottom:1px solid rgba(18,24,32,0.08);'>Total</th>" +
                        "</tr>" +
                      "</thead>" +
                      "<tbody>" + rows + "</tbody>" +
                    "</table>" +
                    "<table role='presentation' width='100%' cellspacing='0' cellpadding='0' style='margin-top:14px;border-collapse:collapse;'>" +
                      "<tr><td></td><td align='right' style='font-size:13px;color:rgba(18,24,32,0.72);padding:4px 0;'>Subtotal: <strong>$" + money(subtotal) + "</strong></td></tr>" +
                      "<tr><td></td><td align='right' style='font-size:13px;color:rgba(18,24,32,0.72);padding:4px 0;'>Tax (10%): <strong>$" + money(tax) + "</strong></td></tr>" +
                      "<tr><td></td><td align='right' style='font-size:15px;color:#121820;padding:8px 0;border-top:1px solid rgba(18,24,32,0.10);'>Total: <strong>$" + money(total) + "</strong></td></tr>" +
                    "</table>" +
                    "<div style='padding:18px 0 2px 0;'>" +
                      "<a href='" + ordersUrl + "' style='display:inline-block;background:#4f46e5;color:#ffffff;text-decoration:none;padding:12px 16px;font-weight:700;font-size:13px;letter-spacing:0.02em;margin-right:10px;'>View my orders</a>" +
                      "<a href='" + invoiceUrl + "' style='display:inline-block;background:#ffffff;color:#121820;text-decoration:none;padding:12px 16px;font-weight:700;font-size:13px;letter-spacing:0.02em;border:1px solid rgba(18,24,32,0.22);'>Download invoice</a>" +
                    "</div>" +
                    "<p style='margin:14px 0 0 0;color:rgba(18,24,32,0.55);font-size:12px;line-height:1.55;'>If the invoice link doesn't work, the PDF is attached to this email.</p>" +
                  "</td></tr>" +
                  "<tr><td style='padding:18px 24px;background:#fbfaf8;border-top:1px solid rgba(18,24,32,0.08);'>" +
                    "<div style='font-size:11px;line-height:1.55;color:rgba(18,24,32,0.55);'>" +
                      "This is a transactional receipt from " + brand + ". For support, contact your administrator." +
                    "</div>" +
                  "</td></tr>" +
                "</table>" +
              "</td></tr>" +
            "</table>" +
            "</body></html>";
    }

    private static String esc(String input) {
        if (input == null) return "";
        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }

    private static String money(BigDecimal value) {
        if (value == null) return "0.00";
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
