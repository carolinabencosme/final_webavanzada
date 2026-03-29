package com.bookstore.notification.service;
import com.bookstore.notification.event.OrderCompletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.*;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service @Slf4j
public class InvoiceService {

    public byte[] generateInvoice(OrderCompletedEvent event) {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                float margin = 50;
                float yPos = 780;
                float pageWidth = PDRectangle.A4.getWidth();

                PDFont bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                PDFont regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

                cs.setNonStrokingColor(79f/255, 70f/255, 229f/255);
                cs.addRect(0, yPos - 10, pageWidth, 60);
                cs.fill();
                cs.setNonStrokingColor(1f,1f,1f);
                cs.beginText(); cs.setFont(bold,24); cs.newLineAtOffset(margin, yPos+20);
                cs.showText("BookStore"); cs.endText();
                cs.beginText(); cs.setFont(regular,12); cs.newLineAtOffset(pageWidth-200, yPos+20);
                cs.showText("INVOICE"); cs.endText();

                yPos -= 60;
                cs.setNonStrokingColor(0f,0f,0f);

                String invoiceNum = "INV-" + (event.getOrderNumber() != null ? event.getOrderNumber() : "N/A");
                String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                drawText(cs, bold, 11, margin, yPos, "Invoice #: " + invoiceNum);
                drawText(cs, regular, 11, pageWidth/2, yPos, "Date: " + date);
                yPos -= 20;
                drawText(cs, bold, 11, margin, yPos, "Customer: " + (event.getUserEmail() != null ? event.getUserEmail() : ""));
                yPos -= 20;
                drawText(cs, regular, 11, margin, yPos, "Order #: " + (event.getOrderNumber() != null ? event.getOrderNumber() : ""));

                yPos -= 30;
                cs.setNonStrokingColor(79f/255, 70f/255, 229f/255);
                cs.addRect(margin, yPos-5, pageWidth-2*margin, 25);
                cs.fill();
                cs.setNonStrokingColor(1f,1f,1f);
                drawText(cs, bold, 10, margin+5, yPos+5, "Book Title");
                drawText(cs, bold, 10, pageWidth-230, yPos+5, "Qty");
                drawText(cs, bold, 10, pageWidth-180, yPos+5, "Price");
                drawText(cs, bold, 10, pageWidth-100, yPos+5, "Total");
                cs.setNonStrokingColor(0f,0f,0f);

                if (event.getItems() != null) {
                    for (OrderCompletedEvent.OrderItemInfo item : event.getItems()) {
                        yPos -= 25;
                        if (yPos < 100) break;
                        String title = item.getBookTitle() != null ? item.getBookTitle() : "Unknown";
                        if (title.length() > 40) title = title.substring(0, 37) + "...";
                        BigDecimal lineTotal = item.getPrice() != null
                            ? item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                            : BigDecimal.ZERO;
                        drawText(cs, regular, 10, margin+5, yPos, title);
                        drawText(cs, regular, 10, pageWidth-230, yPos, String.valueOf(item.getQuantity()));
                        drawText(cs, regular, 10, pageWidth-180, yPos, "$" + (item.getPrice() != null ? item.getPrice() : "0.00"));
                        drawText(cs, regular, 10, pageWidth-100, yPos, "$" + lineTotal);
                    }
                }

                yPos -= 40;
                cs.setStrokingColor(0.8f, 0.8f, 0.8f);
                cs.moveTo(margin, yPos+15); cs.lineTo(pageWidth-margin, yPos+15); cs.stroke();

                BigDecimal total = event.getTotal() != null ? event.getTotal() : BigDecimal.ZERO;
                BigDecimal tax = total.multiply(BigDecimal.valueOf(0.1)).divide(BigDecimal.valueOf(1.1), 2, RoundingMode.HALF_UP);
                BigDecimal subtotal = total.subtract(tax);

                drawText(cs, regular, 11, pageWidth-200, yPos, "Subtotal: $" + subtotal.setScale(2, RoundingMode.HALF_UP));
                yPos -= 20;
                drawText(cs, regular, 11, pageWidth-200, yPos, "Tax (10%): $" + tax.setScale(2, RoundingMode.HALF_UP));
                yPos -= 25;
                cs.setNonStrokingColor(79f/255, 70f/255, 229f/255);
                cs.addRect(pageWidth-210, yPos-5, 170, 22);
                cs.fill();
                cs.setNonStrokingColor(1f,1f,1f);
                drawText(cs, bold, 12, pageWidth-200, yPos+2, "TOTAL: $" + total.setScale(2, RoundingMode.HALF_UP));
                cs.setNonStrokingColor(0f,0f,0f);

                yPos -= 40;
                drawText(cs, regular, 9, margin, yPos, "Thank you for your purchase! | BookStore Platform");
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generating PDF: {}", e.getMessage(), e);
            return generateFallbackPdf(event);
        }
    }

    private void drawText(PDPageContentStream cs, PDFont font, float size, float x, float y, String text) throws Exception {
        cs.beginText(); cs.setFont(font, size); cs.newLineAtOffset(x, y); cs.showText(text); cs.endText();
    }

    private byte[] generateFallbackPdf(OrderCompletedEvent event) {
        String pdf = "%PDF-1.4\n1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\n" +
            "2 0 obj<</Type/Pages/Kids[3 0 R]/Count 1>>endobj\n" +
            "3 0 obj<</Type/Page/MediaBox[0 0 612 792]/Parent 2 0 R/Contents 4 0 R/Resources<</Font<</F1 5 0 R>>>>>>endobj\n" +
            "4 0 obj<</Length 44>>stream\nBT /F1 12 Tf 100 700 Td (Invoice: " +
            (event.getOrderNumber()!=null?event.getOrderNumber():"N/A") + ") Tj ET\nendstream\nendobj\n" +
            "5 0 obj<</Type/Font/Subtype/Type1/BaseFont/Helvetica>>endobj\n" +
            "xref\n0 6\n0000000000 65535 f\n0000000009 00000 n\n0000000058 00000 n\n" +
            "0000000115 00000 n\n0000000266 00000 n\n0000000360 00000 n\n" +
            "trailer<</Size 6/Root 1 0 R>>\nstartxref\n430\n%%EOF";
        return pdf.getBytes();
    }
}
