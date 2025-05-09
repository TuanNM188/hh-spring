package com.formos.huub.service.pdf;

import com.formos.huub.domain.entity.Invoice;
import com.formos.huub.domain.entity.InvoiceDetail;
import com.formos.huub.domain.request.file.ByteArrayMultipartFile;
import com.formos.huub.framework.constant.AppConstants;
import com.formos.huub.framework.enums.DateTimeFormat;
import com.formos.huub.framework.exception.NotFoundException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.utils.DateUtils;
import com.formos.huub.framework.utils.StringUtils;
import com.formos.huub.repository.InvoiceDetailRepository;
import com.formos.huub.repository.InvoiceRepository;
import com.formos.huub.service.file.FileService;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * ***************************************************
 * * Description :
 * * File        : InvoicePdfService
 * * Author      : Hung Tran
 * * Date        : Mar 05, 2025
 * ***************************************************
 **/

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class InvoicePdfService {

    private static final String INVOICE_RESOURCE_KEY = "invoice/%s";
    private static final String INVOICE_PDF_NAME = "%s_%s_%s.pdf";

    // Common colors and borders
    private static final DeviceRgb LIGHT_GRAY = new DeviceRgb(220, 220, 220);
    private static final DeviceRgb BLACK_COLOR = new DeviceRgb(0, 0, 0);
    private static final DeviceRgb DARK_GRAY = new DeviceRgb(50, 50, 50);
    private static final Color WHITE = ColorConstants.WHITE;
    private static final SolidBorder LIGHT_GRAY_BORDER = new SolidBorder(LIGHT_GRAY, 1.2f);
    private static final SolidBorder BLACK_BORDER = new SolidBorder(BLACK_COLOR, 1.25f);

    // Font sizes
    private static final float TITLE_FONT_SIZE = 44f;
    private static final float HEADER_FONT_SIZE = 10f;
    private static final float CONTENT_FONT_SIZE = 11f;
    private static final float TABLE_HEADER_FONT_SIZE = 8f;
    private static final float TABLE_CONTENT_FONT_SIZE = 10f;
    private static final float TABLE_AMOUNT_FONT_SIZE = 12f;
    private static final float FOOTER_FONT_SIZE = 9f;

    // Spacing constants
    private static final float DEFAULT_LEADING = 1.2f;
    private static final float TITLE_LEADING = 1.3f;
    private static final float TITLE_SPACING = 2f;

    private final InvoiceDetailRepository invoiceDetailRepository;
    private final InvoiceRepository invoiceRepository;
    private final FileService fileService;

    /**
     * Generate a PDF invoice and return the file path.
     */
    public void generateInvoicePdf(UUID invoiceId) {
        // Get invoice by id
        Invoice invoice = getInvoiceById(invoiceId);
        generateInvoicePdf(invoice);
    }

    public void generateInvoicePdf(Invoice invoice) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        var advisorName = invoice.getPayToName().replaceAll(AppConstants.KEY_SPACE_REGEX, AppConstants.KEY_EMPTY);
        var monthYear = DateUtils.convertDateToEnglishString(invoice.getDueDate(), DateTimeFormat.MMMMyyyy);
        String fileName = String.format(INVOICE_PDF_NAME, invoice.getInvoiceNumber(), advisorName, monthYear);
        try {
            // Create PDF document
            Document document = createPdfDocument(outputStream);

            // Add content to the PDF
            addInvoiceContent(document, invoice);

            // Close the document
            document.close();

            MultipartFile pdfFile = new ByteArrayMultipartFile(outputStream.toByteArray(), "file", fileName, "application/pdf");
            String pdfUrl = fileService.uploadPrivateFile(pdfFile, String.format(INVOICE_RESOURCE_KEY, invoice.getInvoiceNumber()));
            log.info("Generated invoice PDF at: {}", pdfUrl);
            invoice.setFileName(fileName);
            invoice.setFilePath(pdfUrl);
            invoiceRepository.save(invoice);
        } catch (Exception e) {
            log.error("Error generating invoice PDF", e);
        }
    }

    /**
     * Get invoice by ID or throw NotFoundException.
     */
    private Invoice getInvoiceById(UUID invoiceId) {
        return invoiceRepository
            .findById(invoiceId)
            .orElseThrow(() -> new NotFoundException(MessageHelper.getMessage(Message.Keys.E0010, "Invoice")));
    }

    /**
     * Create and configure the PDF document.
     */
    private Document createPdfDocument(ByteArrayOutputStream outputStream) {
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdf = new PdfDocument(writer);
        pdf.setDefaultPageSize(PageSize.LETTER);
        Document document = new Document(pdf);
        document.setMargins(130, 80, 10, 90); // 0.5 inch margins
        return document;
    }

    /**
     * Add content to the invoice PDF.
     */
    private void addInvoiceContent(Document document, Invoice invoice) throws IOException {
        // Load fonts
        PdfFont fontNormal = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

        // Add invoice title
        addInvoiceTitle(document, fontBold);

        // Add invoice info section (invoice # and due date)
        addInvoiceInfoSection(document, invoice, fontNormal, fontBold);

        // Add spacing
        addSpacing(document, 34);

        // Add billing and payment info
        addBillingAndPaymentInfo(document, invoice, fontNormal, fontBold);

        // Add spacing
        addSpacing(document, 20);

        // Add invoice items table
        addInvoiceTable(document, invoice, fontNormal, fontBold);

        // Add footer
        addFooter(document, fontNormal);
    }

    /**
     * Add the invoice title to the document.
     */
    private void addInvoiceTitle(Document document, PdfFont fontBold) {
        Paragraph title = new Paragraph("Invoice");
        title.setFont(fontBold);
        title.setFontSize(TITLE_FONT_SIZE);
        title.setTextAlignment(TextAlignment.LEFT);
        title.setFontColor(DARK_GRAY);
        title.setPaddingBottom(0);
        title.setMarginBottom(0);
        title.setPaddingLeft(1);
        title.setMultipliedLeading(TITLE_LEADING);
        title.setCharacterSpacing(TITLE_SPACING);
        document.add(title);
    }

    /**
     * Add invoice info section (invoice # and due date).
     */
    private void addInvoiceInfoSection(Document document, Invoice invoice, PdfFont fontNormal, PdfFont fontBold) {
        Table invoiceInfoTable = new Table(UnitValue.createPercentArray(new float[] { 30, 70 }))
            .setWidth(UnitValue.createPercentValue(45))
            .setBorder(Border.NO_BORDER)
            .setMargin(0)
            .setPadding(0);

        // INVOICE # section
        addLabelAndValueCells(invoiceInfoTable, "INVOICE #", invoice.getInvoiceNumber(), fontBold, fontNormal, 0, 10);

        // DUE DATE section
        String formattedDueDate = formatDate(invoice.getDueDate());
        addLabelAndValueCells(invoiceInfoTable, "DUE DATE:", formattedDueDate, fontBold, fontNormal, 2.7f, 0);

        document.add(invoiceInfoTable);
    }

    /**
     * Format date to MM/dd/yy.
     */
    private String formatDate(Instant date) {
        if (date == null) {
            return "";
        }
        return DateTimeFormatter.ofPattern(DateTimeFormat.MM_DD_YY.getValue()).withZone(ZoneId.systemDefault()).format(date);
    }

    /**
     * Add label and value cells to a table.
     */
    private void addLabelAndValueCells(Table table, String labelText, String valueText, PdfFont fontBold,
                                       PdfFont fontNormal, float labelLeading, float marginBottom) {
        Cell labelCell = new Cell()
            .add(new Paragraph(labelText)
                .setFont(fontBold)
                .setFontSize(HEADER_FONT_SIZE)
                .setMultipliedLeading(labelLeading)
            )
            .setBorder(Border.NO_BORDER)
            .setPadding(0)
            .setMargin(0);
        table.addCell(labelCell);

        Cell valueCell = new Cell()
            .add(new Paragraph(valueText)
                .setFont(fontNormal)
                .setFontSize(CONTENT_FONT_SIZE)
                .setMultipliedLeading(labelLeading == 0 ? 0 : labelLeading - 0.2f)
            )
            .setBorder(Border.NO_BORDER)
            .setPadding(0)
            .setMargin(0);

        if (marginBottom > 0) {
            valueCell.setMarginBottom(marginBottom);
        }

        table.addCell(valueCell);
    }

    /**
     * Add billing and payment information section.
     */
    private void addBillingAndPaymentInfo(Document document, Invoice invoice, PdfFont fontNormal, PdfFont fontBold) {
        Table infoTable = new Table(UnitValue.createPercentArray(new float[] { 44, 56 }))
            .setWidth(UnitValue.createPercentValue(100))
            .setBorder(Border.NO_BORDER)
            .setMargin(0)
            .setPadding(0);

        // Add header cells
        addSectionHeaderCell(infoTable, "BILL TO:", fontBold);
        addSectionHeaderCell(infoTable, "PAY TO:", fontBold);

        // BILL TO content
        String billToContent = formatAddressContent(
            invoice.getBillToName(),
            invoice.getBillToAddress(),
            invoice.getBillToCity(),
            invoice.getBillToState(),
            invoice.getBillToZip(),
            null
        );
        addContentCell(infoTable, billToContent, fontNormal, 10f);

        // PAY TO content
        String payToContent = formatAddressContent(
            invoice.getPayToName(),
            invoice.getPayToAddress(),
            invoice.getPayToCity(),
            invoice.getPayToState(),
            invoice.getPayToZip(),
            invoice.getPayToPhone()
        );
        addContentCell(infoTable, payToContent, fontNormal, 0);

        document.add(infoTable);
    }

    /**
     * Format address content for billing and payment sections.
     */
    private String formatAddressContent(String name, String address, String city, String state, String zip, String phone) {
        StringBuilder content = new StringBuilder();
        content.append(name).append("\n");

        // Address line
        String comma = "";
        if (address != null && !address.isEmpty()) {
            content.append(address);
            comma = ", ";
        }

        // City, State Zip
        if (city != null && !city.isEmpty()) {
            content.append(comma).append(city);
        }
        if (state != null && !state.isEmpty()) {
            content.append(comma).append(state);
        }
        if (zip != null && !zip.isEmpty()) {
            content.append(" ").append(zip);
        }

        // Phone (if provided)
        if (phone != null && !phone.isEmpty()) {
            content.append("\n").append(phone);
        }

        return content.toString();
    }

    /**
     * Add section header cell to a table.
     */
    private void addSectionHeaderCell(Table table, String headerText, PdfFont fontBold) {
        Cell headerCell = new Cell()
            .add(new Paragraph(headerText)
                .setFont(fontBold)
                .setFontSize(HEADER_FONT_SIZE)
            )
            .setBorder(Border.NO_BORDER)
            .setMargin(0)
            .setPadding(0)
            .setPaddingBottom(8f);
        table.addCell(headerCell);
    }

    /**
     * Add content cell to a table.
     */
    private void addContentCell(Table table, String content, PdfFont fontNormal, float paddingRight) {
        Cell contentCell = new Cell()
            .add(new Paragraph(content)
                .setFont(fontNormal)
                .setFontSize(CONTENT_FONT_SIZE)
                .setMultipliedLeading(DEFAULT_LEADING)
            )
            .setBorder(Border.NO_BORDER)
            .setMargin(0)
            .setPadding(0);

        if (paddingRight > 0) {
            contentCell.setPaddingRight(paddingRight);
        }

        table.addCell(contentCell);
    }

    /**
     * Add spacing paragraph with specified margin.
     */
    private void addSpacing(Document document, float marginBottom) {
        document.add(new Paragraph("").setMarginBottom(marginBottom));
    }

    /**
     * Add invoice items table to the document.
     */
    private void addInvoiceTable(Document document, Invoice invoice, PdfFont fontNormal, PdfFont fontBold) {
        // Create table with 5 columns
        Table table = new Table(UnitValue.createPercentArray(new float[] { 12, 52, 13, 10, 13 }))
            .setWidth(UnitValue.createPercentValue(100));

        // Add header cells
        table.addHeaderCell(createHeaderCell("ITEMS", fontBold));
        table.addHeaderCell(createHeaderCell("DESCRIPTION", fontBold));
        table.addHeaderCell(createHeaderCell("QUANTITY", fontBold));
        table.addHeaderCell(createHeaderCell("PRICE", fontBold));
        table.addHeaderCell(createHeaderCell("AMOUNT", fontBold));

        // Add invoice details
        addInvoiceDetailRows(table, invoice, fontNormal);

        // Add black divider line
        addBlackDividerLine(table);

        // Add total row
        addTotalRow(table, invoice, fontBold);

        document.add(table);
    }

    /**
     * Create a header cell for the invoice table.
     */
    private Cell createHeaderCell(String text, PdfFont fontBold) {
        return new Cell()
            .add(new Paragraph(text)
                .setFont(fontBold)
                .setFontSize(TABLE_HEADER_FONT_SIZE)
                .setFontColor(WHITE)
            )
            .setBackgroundColor(BLACK_COLOR)
            .setPadding(3)
            .setBorder(LIGHT_GRAY_BORDER);
    }

    /**
     * Add invoice detail rows to the table.
     */
    private void addInvoiceDetailRows(Table table, Invoice invoice, PdfFont fontNormal) {
        List<InvoiceDetail> details = invoiceDetailRepository.findByInvoiceId(invoice.getId());

        for (InvoiceDetail detail : details) {
            // Item name
            addItemNameCell(table, detail.getItemName(), fontNormal);

            // Description
            addDescriptionCell(table, detail.getDescription(), fontNormal);

            // Quantity
            addAmountCell(table, detail.getTotalHours().toString(), fontNormal);

            // Price
            addAmountCell(table, StringUtils.convertToUSCurrency(detail.getPrice()), fontNormal);

            // Amount
            addAmountCell(table, StringUtils.convertToUSCurrency(detail.getAmount()), fontNormal);
        }
    }

    /**
     * Add item name cell to the table.
     */
    private void addItemNameCell(Table table, String itemName, PdfFont fontNormal) {
        Cell cell = new Cell()
            .add(new Paragraph(itemName)
                .setFont(fontNormal)
                .setFontSize(TABLE_CONTENT_FONT_SIZE)
                .setMultipliedLeading(1f)
            )
            .setPadding(5)
            .setBorder(LIGHT_GRAY_BORDER);
        table.addCell(cell);
    }

    /**
     * Add description cell to the table.
     */
    private void addDescriptionCell(Table table, String descriptionText, PdfFont fontNormal) {
        // Parse description
        List<String> descriptionLines = new ArrayList<>();
        if (descriptionText != null && !descriptionText.isEmpty()) {
            if (descriptionText.contains("\n")) {
                descriptionLines.addAll(Arrays.asList(descriptionText.split("\n")));
            } else {
                descriptionLines.add(descriptionText);
            }
        }

        // Create paragraph with the formatted description
        Paragraph descriptionParagraph = new Paragraph();
        for (int i = 0; i < descriptionLines.size(); i++) {
            descriptionParagraph.add(new Text(descriptionLines.get(i))
                .setFont(fontNormal)
                .setFontSize(CONTENT_FONT_SIZE)
            );

            // Add newline if not the last line
            if (i < descriptionLines.size() - 1) {
                descriptionParagraph.add(new Text("\n"));
            }
        }

        Cell cell = new Cell()
            .add(descriptionParagraph.setMultipliedLeading(1.1f))
            .setPadding(5)
            .setBorder(LIGHT_GRAY_BORDER);
        table.addCell(cell);
    }

    /**
     * Add amount cell (used for quantity, price, and amount).
     */
    private void addAmountCell(Table table, String value, PdfFont fontNormal) {
        Cell cell = new Cell()
            .add(new Paragraph(value)
                .setFont(fontNormal)
                .setFontSize(TABLE_AMOUNT_FONT_SIZE)
            )
            .setPadding(5)
            .setPaddingTop(30)
            .setTextAlignment(TextAlignment.LEFT)
            .setBorder(LIGHT_GRAY_BORDER);
        table.addCell(cell);
    }

    /**
     * Add black divider line to the table.
     */
    private void addBlackDividerLine(Table table) {
        Cell blackLineCell = new Cell(1, 5)
            .setBorderBottom(BLACK_BORDER)
            .setBorderTop(BLACK_BORDER)
            .setHeight(0) // Make it slightly taller to be more visible
            .setPadding(0);
        table.addCell(blackLineCell);
    }

    /**
     * Add total row to the table.
     */
    private void addTotalRow(Table table, Invoice invoice, PdfFont fontBold) {
        // Empty cells
        table.addCell(createEmptyTotalCell());
        table.addCell(createEmptyTotalCell());

        // Total label (spanning 2 columns)
        Cell totalLabelCell = new Cell(1, 2)
            .add(new Paragraph("TOTAL DUE")
                .setFont(fontBold)
                .setFontSize(HEADER_FONT_SIZE)
            )
            .setPaddings(17, 0, 15, 5)
            .setTextAlignment(TextAlignment.LEFT)
            .setBorder(LIGHT_GRAY_BORDER)
            .setBorderTop(Border.NO_BORDER);
        table.addCell(totalLabelCell);

        // Total amount
        Cell totalAmountCell = new Cell()
            .add(new Paragraph(StringUtils.convertToUSCurrency(invoice.getTotalAmount()))
                .setFontSize(TABLE_AMOUNT_FONT_SIZE)
            )
            .setPaddings(17, 5, 10, 5)
            .setTextAlignment(TextAlignment.LEFT)
            .setBorder(LIGHT_GRAY_BORDER)
            .setBorderTop(Border.NO_BORDER);
        table.addCell(totalAmountCell);
    }

    /**
     * Create empty cell for total row.
     */
    private Cell createEmptyTotalCell() {
        return new Cell()
            .setBorder(LIGHT_GRAY_BORDER)
            .setBorderTop(Border.NO_BORDER);
    }

    /**
     * Add footer to the document.
     */
    private void addFooter(Document document, PdfFont fontNormal) {
        PdfDocument pdfDoc = document.getPdfDocument();
        PdfCanvas pdfCanvas = new PdfCanvas(pdfDoc.getLastPage());
        Rectangle pageSize = pdfDoc.getLastPage().getPageSize();

        try (Canvas canvas = new Canvas(pdfCanvas, pageSize)) {
            canvas.showTextAligned(
                new Paragraph(AppConstants.COMPANY_WEBSITE + " - " + AppConstants.COMPANY_TAGLINE)
                    .setFont(fontNormal)
                    .setFontSize(FOOTER_FONT_SIZE),
                pageSize.getWidth() / 2,
                20,
                TextAlignment.CENTER
            );
        }
    }
}
