package com.bookstore.notification.service;

import com.bookstore.notification.event.OrderCompletedEvent;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class InvoiceService {

    private volatile JasperReport compiledReport;

    public byte[] generateInvoice(OrderCompletedEvent event) {
        try {
            JasperReport report = getOrCompileReport();
            Map<String, Object> params = new HashMap<>();
            String inv = event.getOrderNumber() != null ? event.getOrderNumber() : "INV-" + event.getOrderId();
            params.put("ORDER_NUMBER", inv);
            params.put("CUSTOMER_EMAIL", event.getUserEmail() != null ? event.getUserEmail() : "");
            params.put("ORDER_DATE", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

            BigDecimal total = event.getTotal() != null ? event.getTotal() : BigDecimal.ZERO;
            BigDecimal tax = total.multiply(BigDecimal.valueOf(0.1)).divide(BigDecimal.valueOf(1.1), 2, RoundingMode.HALF_UP);
            BigDecimal subtotal = total.subtract(tax);

            params.put("SUBTOTAL", "$" + subtotal.setScale(2, RoundingMode.HALF_UP));
            params.put("TAX", "$" + tax.setScale(2, RoundingMode.HALF_UP));
            params.put("TOTAL", "$" + total.setScale(2, RoundingMode.HALF_UP));

            List<InvoiceLineRow> rows = new ArrayList<>();
            if (event.getItems() != null) {
                for (OrderCompletedEvent.OrderItemInfo item : event.getItems()) {
                    BigDecimal price = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
                    int qty = item.getQuantity();
                    BigDecimal line = price.multiply(BigDecimal.valueOf(qty));
                    rows.add(new InvoiceLineRow(
                        truncate(item.getBookTitle(), 80),
                        qty,
                        "$" + price.setScale(2, RoundingMode.HALF_UP),
                        "$" + line.setScale(2, RoundingMode.HALF_UP)
                    ));
                }
            }
            if (rows.isEmpty()) {
                rows.add(new InvoiceLineRow("(no line items)", 0, "$0.00", "$0.00"));
            }

            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);
            JasperPrint print = JasperFillManager.fillReport(report, params, dataSource);
            return JasperExportManager.exportReportToPdf(print);
        } catch (Exception e) {
            log.error("Jasper invoice error: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate invoice PDF", e);
        }
    }

    private JasperReport getOrCompileReport() throws JRException {
        if (compiledReport != null) {
            return compiledReport;
        }
        synchronized (this) {
            if (compiledReport != null) {
                return compiledReport;
            }
            ClassPathResource res = new ClassPathResource("reports/invoice.jrxml");
            try (InputStream in = res.getInputStream()) {
                compiledReport = JasperCompileManager.compileReport(in);
            } catch (java.io.IOException e) {
                throw new JRException("Could not load invoice.jrxml", e);
            }
            return compiledReport;
        }
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }

    /** Public getters for Jasper bean datasource */
    public static class InvoiceLineRow {
        private final String bookTitle;
        private final Integer quantity;
        private final String unitPrice;
        private final String lineTotal;

        public InvoiceLineRow(String bookTitle, Integer quantity, String unitPrice, String lineTotal) {
            this.bookTitle = bookTitle;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.lineTotal = lineTotal;
        }

        public String getBookTitle() { return bookTitle; }
        public Integer getQuantity() { return quantity; }
        public String getUnitPrice() { return unitPrice; }
        public String getLineTotal() { return lineTotal; }
    }
}
