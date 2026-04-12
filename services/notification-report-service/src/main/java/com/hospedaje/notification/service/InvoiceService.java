package com.hospedaje.notification.service;

import com.hospedaje.notification.event.OrderCompletedEvent;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
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

    /** Paleta alineada con la marca Luma (frontend). */
    private static final Color PDF_HEADER_BG = new Color(30, 41, 59);
    private static final Color PDF_ACCENT = new Color(79, 70, 229);
    private static final Color PDF_ROW_STRIPE = new Color(248, 250, 252);
    private static final Color PDF_BORDER = new Color(226, 232, 240);
    private static final Color PDF_MUTED = new Color(100, 116, 139);
    private static final Color PDF_INK = new Color(15, 23, 42);

    private volatile JasperReport compiledReport;

    public byte[] generateInvoice(OrderCompletedEvent event) {
        log.info("invoice_generate_requested orderId={} orderNumber={} items={} total={}",
            event.getOrderId(),
            event.getOrderNumber(),
            event.getItems() == null ? 0 : event.getItems().size(),
            event.getTotal());

        List<InvoiceLineRow> rows = buildLineRows(event);
        if (rows.isEmpty()) {
            addFallbackLine(event, rows);
        }
        if (rows.isEmpty()) {
            throw new IllegalStateException("Invoice has no line items for orderId=" + event.getOrderId());
        }

        BigDecimal total = event.getTotal() != null ? event.getTotal() : BigDecimal.ZERO;
        BigDecimal tax = total.multiply(BigDecimal.valueOf(0.1)).divide(BigDecimal.valueOf(1.1), 2, RoundingMode.HALF_UP);
        BigDecimal subtotal = total.subtract(tax);

        String inv = event.getOrderNumber() != null ? event.getOrderNumber() : "INV-" + event.getOrderId();
        Map<String, Object> params = buildJasperParams(event, inv, subtotal, tax, total);

        try {
            JasperReport report = getOrCompileReport();
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);
            JasperPrint print = JasperFillManager.fillReport(report, params, dataSource);
            return JasperExportManager.exportReportToPdf(print);
        } catch (Exception e) {
            log.warn("jasper_pdf_export_failed orderId={} msg={} — using OpenPDF fallback",
                event.getOrderId(), e.getMessage());
            return buildOpenPdfInvoice(event, rows, subtotal, tax, total);
        }
    }

    private Map<String, Object> buildJasperParams(
        OrderCompletedEvent event,
        String inv,
        BigDecimal subtotal,
        BigDecimal tax,
        BigDecimal total
    ) {
        Map<String, Object> params = new HashMap<>();
        putStr(params, "ORDER_NUMBER", inv);
        putStr(params, "CUSTOMER_EMAIL", event.getUserEmail());
        putStr(params, "ORDER_DATE", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        putStr(params, "SUBTOTAL", "$" + subtotal.setScale(2, RoundingMode.HALF_UP));
        putStr(params, "TAX", "$" + tax.setScale(2, RoundingMode.HALF_UP));
        putStr(params, "TOTAL", "$" + total.setScale(2, RoundingMode.HALF_UP));
        putStr(params, "PROPERTY_NAME", nz(event.getPropertyName()));
        putStr(params, "CITY", nz(event.getCity()));
        putStr(params, "CHECK_IN", event.getCheckIn() != null ? event.getCheckIn().toString() : "");
        putStr(params, "CHECK_OUT", event.getCheckOut() != null ? event.getCheckOut().toString() : "");
        return params;
    }

    private List<InvoiceLineRow> buildLineRows(OrderCompletedEvent event) {
        List<InvoiceLineRow> rows = new ArrayList<>();
        if (event.getItems() != null) {
            for (OrderCompletedEvent.OrderItemInfo item : event.getItems()) {
                if (item == null) {
                    continue;
                }
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
        return rows;
    }

    private byte[] buildOpenPdfInvoice(
        OrderCompletedEvent event,
        List<InvoiceLineRow> rows,
        BigDecimal subtotal,
        BigDecimal tax,
        BigDecimal total
    ) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4, 40, 40, 36, 40);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            String num = event.getOrderNumber() != null ? event.getOrderNumber() : ("INV-" + event.getOrderId());
            String issued = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));

            doc.add(buildPdfHeaderBand());
            doc.add(new Paragraph(" "));
            doc.add(buildPdfMetaGrid(num, nz(event.getUserEmail()), issued));
            doc.add(new Paragraph(" "));
            doc.add(buildPdfStayCard(event));
            doc.add(new Paragraph(" "));
            doc.add(buildPdfLineItemsTable(rows));
            doc.add(new Paragraph(" "));
            doc.add(buildPdfTotalsBlock(subtotal, tax, total));
            doc.add(Chunk.NEWLINE);
            doc.add(buildPdfFooter());

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("openpdf_invoice_failed orderId={}", event.getOrderId(), e);
            throw new RuntimeException("Failed to generate invoice PDF", e);
        }
    }

    private static PdfPTable buildPdfHeaderBand() {
        Font wordmark = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22f, Color.WHITE);
        Font subtitle = FontFactory.getFont(FontFactory.HELVETICA, 9f, new Color(226, 232, 240));
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100f);
        PdfPCell c = new PdfPCell();
        c.setBorder(Rectangle.NO_BORDER);
        c.setBackgroundColor(PDF_HEADER_BG);
        c.setPadding(20);
        c.setMinimumHeight(72f);
        Paragraph p1 = new Paragraph("LUMA", wordmark);
        p1.setSpacingAfter(4f);
        Paragraph p2 = new Paragraph("FACTURA DE RESERVA · HOSPEDAJE", subtitle);
        c.addElement(p1);
        c.addElement(p2);
        PdfPCell bar = new PdfPCell();
        bar.setBorder(Rectangle.NO_BORDER);
        bar.setBackgroundColor(PDF_ACCENT);
        bar.setFixedHeight(4f);
        t.addCell(c);
        t.addCell(bar);
        return t;
    }

    private static PdfPTable buildPdfMetaGrid(String invoiceNumber, String email, String issuedAt) {
        Font label = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8f, PDF_MUTED);
        Font val = FontFactory.getFont(FontFactory.HELVETICA, 10f, PDF_INK);
        PdfPTable grid = new PdfPTable(2);
        grid.setWidthPercentage(100f);
        grid.setWidths(new float[]{1f, 1f});

        PdfPCell left = new PdfPCell();
        left.setBorder(Rectangle.NO_BORDER);
        left.setPadding(0);
        left.addElement(new Paragraph("NÚMERO DE FACTURA", label));
        left.addElement(new Paragraph("#" + invoiceNumber, val));

        PdfPCell right = new PdfPCell();
        right.setBorder(Rectangle.NO_BORDER);
        right.setPadding(0);
        right.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Paragraph pr = new Paragraph();
        pr.setAlignment(Element.ALIGN_RIGHT);
        pr.add(new Chunk("FECHA DE EMISIÓN\n", label));
        pr.add(new Chunk(issuedAt, val));
        right.addElement(pr);

        grid.addCell(left);
        grid.addCell(right);

        PdfPCell full = new PdfPCell();
        full.setColspan(2);
        full.setBorder(Rectangle.NO_BORDER);
        full.setPaddingTop(12f);
        full.addElement(new Paragraph("HUÉSPED", label));
        full.addElement(new Paragraph(email.isEmpty() ? "—" : email, val));
        grid.addCell(full);

        return grid;
    }

    private static PdfPTable buildPdfStayCard(OrderCompletedEvent event) {
        Font h = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9f, PDF_ACCENT);
        Font body = FontFactory.getFont(FontFactory.HELVETICA, 10f, PDF_INK);
        Font small = FontFactory.getFont(FontFactory.HELVETICA, 9f, PDF_MUTED);

        PdfPTable wrap = new PdfPTable(1);
        wrap.setWidthPercentage(100f);
        PdfPCell shell = new PdfPCell();
        shell.setBorder(Rectangle.BOX);
        shell.setBorderColor(PDF_BORDER);
        shell.setBackgroundColor(PDF_ROW_STRIPE);
        shell.setPadding(14);

        String prop = nz(event.getPropertyName());
        String city = nz(event.getCity());
        shell.addElement(new Paragraph("DETALLE DE LA ESTADÍA", h));
        shell.addElement(new Paragraph((prop + (city.isEmpty() ? "" : " · " + city)).trim(), body));
        shell.addElement(new Paragraph(" ", small));

        PdfPTable dates = new PdfPTable(2);
        dates.setWidthPercentage(100f);
        dates.setWidths(new float[]{1f, 1f});
        addDateCell(dates, "Check-in", event.getCheckIn() != null ? event.getCheckIn().toString() : "—");
        addDateCell(dates, "Check-out", event.getCheckOut() != null ? event.getCheckOut().toString() : "—");
        shell.addElement(dates);

        wrap.addCell(shell);
        return wrap;
    }

    private static void addDateCell(PdfPTable dates, String label, String value) {
        Font lab = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8f, PDF_MUTED);
        Font val = FontFactory.getFont(FontFactory.HELVETICA, 10f, PDF_INK);
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(4);
        cell.addElement(new Paragraph(label.toUpperCase(), lab));
        cell.addElement(new Paragraph(value, val));
        dates.addCell(cell);
    }

    private static PdfPTable buildPdfLineItemsTable(List<InvoiceLineRow> rows) {
        Font th = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8f, Color.WHITE);
        Font td = FontFactory.getFont(FontFactory.HELVETICA, 9f, PDF_INK);
        Font tdMuted = FontFactory.getFont(FontFactory.HELVETICA, 8f, PDF_MUTED);

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100f);
        table.setWidths(new float[]{4.2f, 1f, 1.3f, 1.3f});
        table.setHeaderRows(1);

        String[] heads = {"Concepto", "Noches", "Precio / noche", "Importe" };
        for (String h : heads) {
            PdfPCell hc = new PdfPCell(new Phrase(h, th));
            hc.setBackgroundColor(PDF_HEADER_BG);
            hc.setPadding(8);
            hc.setBorderColor(PDF_BORDER);
            table.addCell(hc);
        }

        int i = 0;
        for (InvoiceLineRow r : rows) {
            Color bg = (i % 2 == 0) ? Color.WHITE : PDF_ROW_STRIPE;
            i++;
            table.addCell(pdfDataCell(nz(r.getBookTitle()), td, bg, Element.ALIGN_LEFT));
            table.addCell(pdfDataCell(String.valueOf(r.getQuantity()), td, bg, Element.ALIGN_CENTER));
            table.addCell(pdfDataCell(nz(r.getUnitPrice()), tdMuted, bg, Element.ALIGN_RIGHT));
            table.addCell(pdfDataCell(nz(r.getLineTotal()), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9f, PDF_INK), bg, Element.ALIGN_RIGHT));
        }
        return table;
    }

    private static PdfPCell pdfDataCell(String text, Font font, Color bg, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setPadding(8);
        cell.setBorderColor(PDF_BORDER);
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    private static PdfPTable buildPdfTotalsBlock(BigDecimal subtotal, BigDecimal tax, BigDecimal total) {
        Font lab = FontFactory.getFont(FontFactory.HELVETICA, 9f, PDF_MUTED);
        Font num = FontFactory.getFont(FontFactory.HELVETICA, 10f, PDF_INK);
        Font numBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14f, PDF_ACCENT);

        PdfPTable inner = new PdfPTable(2);
        inner.setWidths(new float[]{1.4f, 1f});
        inner.setWidthPercentage(100f);

        addTotalRow(inner, "Subtotal", "$" + subtotal.setScale(2, RoundingMode.HALF_UP), lab, num, false);
        addTotalRow(inner, "Impuesto ITBIS (10%)", "$" + tax.setScale(2, RoundingMode.HALF_UP), lab, num, false);

        PdfPCell sep = new PdfPCell(new Phrase(""));
        sep.setColspan(2);
        sep.setBorder(Rectangle.TOP);
        sep.setBorderColor(PDF_BORDER);
        sep.setPaddingTop(8f);
        sep.setPaddingBottom(4f);
        inner.addCell(sep);

        addTotalRow(inner, "TOTAL", "$" + total.setScale(2, RoundingMode.HALF_UP), lab, numBold, true);

        PdfPTable wrap = new PdfPTable(2);
        wrap.setWidthPercentage(100f);
        wrap.setWidths(new float[]{1.2f, 1f});

        PdfPCell spacer = new PdfPCell(new Phrase(""));
        spacer.setBorder(Rectangle.NO_BORDER);

        PdfPCell box = new PdfPCell(inner);
        box.setBorder(Rectangle.NO_BORDER);
        box.setPadding(0);

        wrap.addCell(spacer);
        wrap.addCell(box);
        return wrap;
    }

    private static void addTotalRow(
        PdfPTable inner,
        String label,
        String amount,
        Font labelFont,
        Font amountFont,
        boolean emphasize
    ) {
        PdfPCell l = new PdfPCell(new Phrase(label, emphasize ? FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10f, PDF_INK) : labelFont));
        l.setBorder(Rectangle.NO_BORDER);
        l.setPadding(4);
        l.setHorizontalAlignment(Element.ALIGN_RIGHT);
        PdfPCell a = new PdfPCell(new Phrase(amount, amountFont));
        a.setBorder(Rectangle.NO_BORDER);
        a.setPadding(4);
        a.setHorizontalAlignment(Element.ALIGN_RIGHT);
        inner.addCell(l);
        inner.addCell(a);
    }

    private static Paragraph buildPdfFooter() {
        Font tiny = FontFactory.getFont(FontFactory.HELVETICA, 8f, PDF_MUTED);
        Paragraph p = new Paragraph(
            "Documento generado electrónicamente por Luma · No válido como comprobante fiscal salvo donde aplique la ley.\n"
                + "Gracias por confiar en nosotros para tu estadía.",
            tiny
        );
        p.setAlignment(Element.ALIGN_CENTER);
        p.setSpacingBefore(16f);
        return p;
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

    private static String nz(String s) {
        return s != null ? s : "";
    }

    /** Jasper concatena con + en Java: el operando izquierdo no puede ser null. */
    private static void putStr(Map<String, Object> params, String key, String value) {
        params.put(key, value != null ? value : "");
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }

    /**
     * Some flows (o mensajes Rabbit con ítems vacíos) no dejan líneas; generamos una línea desde totales y nombre de propiedad.
     */
    private void addFallbackLine(OrderCompletedEvent event, List<InvoiceLineRow> rows) {
        BigDecimal total = event.getTotal() != null ? event.getTotal() : BigDecimal.ZERO;
        int nights = event.getNights() > 0 ? event.getNights() : 1;
        BigDecimal price = total.compareTo(BigDecimal.ZERO) > 0
            ? total.divide(BigDecimal.valueOf(nights), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
        String label = event.getPropertyName() != null && !event.getPropertyName().isBlank()
            ? event.getPropertyName()
            : "Reserva de hospedaje";
        rows.add(new InvoiceLineRow(
            truncate(label, 80),
            nights,
            "$" + price.setScale(2, RoundingMode.HALF_UP),
            "$" + total.setScale(2, RoundingMode.HALF_UP)
        ));
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

        public String getBookTitle() {
            return bookTitle;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public String getUnitPrice() {
            return unitPrice;
        }

        public String getLineTotal() {
            return lineTotal;
        }
    }
}
