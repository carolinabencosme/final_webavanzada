package com.bookstore.notification.service;
import com.bookstore.notification.event.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
@Service @Slf4j
public class PdfService {
    public byte[] generateInvoice(OrderCreatedEvent event) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                PDType1Font bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                PDType1Font regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                cs.beginText();
                cs.setFont(bold, 20);
                cs.newLineAtOffset(50, 750);
                cs.showText("BookStore - Order Invoice");
                cs.endText();

                cs.beginText();
                cs.setFont(regular, 12);
                cs.newLineAtOffset(50, 720);
                cs.showText("Order ID: " + event.getOrderId());
                cs.newLineAtOffset(0, -20);
                cs.showText("Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                cs.newLineAtOffset(0, -20);
                cs.showText("Customer: " + event.getUserEmail());
                cs.endText();

                cs.beginText();
                cs.setFont(bold, 12);
                cs.newLineAtOffset(50, 640);
                cs.showText("Items:");
                cs.endText();

                int yPos = 620;
                if (event.getItems() != null) {
                    for (OrderCreatedEvent.OrderItemEvent item : event.getItems()) {
                        cs.beginText();
                        cs.setFont(regular, 11);
                        cs.newLineAtOffset(50, yPos);
                        BigDecimal lineTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                        cs.showText(item.getBookTitle() + " x" + item.getQuantity() + "  $" + lineTotal);
                        cs.endText();
                        yPos -= 18;
                    }
                }

                cs.beginText();
                cs.setFont(bold, 13);
                cs.newLineAtOffset(50, yPos - 20);
                cs.showText("Total: $" + event.getTotal());
                cs.endText();
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Error generating PDF invoice: {}", e.getMessage());
            return new byte[0];
        }
    }
}
