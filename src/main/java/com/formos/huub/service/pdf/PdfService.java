package com.formos.huub.service.pdf;

import com.formos.huub.config.ApplicationProperties;
import com.formos.huub.domain.constant.FormConstant;
import com.formos.huub.domain.entity.*;
import com.formos.huub.domain.enums.AppointmentStatusEnum;
import com.formos.huub.domain.enums.EntityTypeEnum;
import com.formos.huub.domain.enums.ProjectStatusEnum;
import com.formos.huub.domain.enums.RoleEnum;
import com.formos.huub.domain.request.file.ByteArrayMultipartFile;
import com.formos.huub.framework.enums.DateTimeFormat;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.utils.DateUtils;
import com.formos.huub.framework.utils.StringUtils;
import com.formos.huub.repository.*;
import com.formos.huub.service.file.FileService;
import com.formos.huub.service.useranswerform.UserFormService;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PdfService {

    private static final float MARGIN = 36;
    private static final float PADDING = 12;
    private static final float FONT_SIZE = 10;
    private static final float LOGO_WIDTH = 156;
    private static final float LOGO_HEIGHT = 34;
    private static final float WRAP_HEADER_WIDTH = 350;
    private static final DeviceRgb LIGHT_GRAY = new DeviceRgb(240, 240, 240);
    private static final DeviceRgb GRAY_BORDER = new DeviceRgb(200, 200, 200);
    private static final Border BORDER = new SolidBorder(GRAY_BORDER, 1);

    private static final String TIMEZONE = "America/Los_Angeles";
    private static final String LOGO_PATH = "static/logo.png";
    private static final String CLIENT_APP_BASE_URL = "myhuub.com";
    private static final String FOOTER_SOLOGAN = "YOUR ONE PLACE TO ACCESS THE WORLD OF ENTREPRENEURSHIP";
    private static final String PROJECT_REPORT_FOR = "Project Report for";
    private static final String APPOINTMENT_REPORT_FOR = "Appointment Report for";
    private static final String PROJECT_REPORT_RESOURCE_KEY = "projectReport/summary";
    private static final String APPOINTMENT_REPORT_RESOURCE_KEY = "appointmentReport/summary";

    private static final String PDF_FILENAME = "PDF_FILENAME";
    private static final String PDF_PATH = "PDF_PATH";

    ProjectRepository projectRepository;
    CategoryRepository categoryRepository;
    AppointmentReportRepository appointmentReportRepository;
    ServiceOutcomeRepository serviceOutcomeRepository;
    EntityAttachmentRepository entityAttachmentRepository;
    ProjectReportRepository projectReportRepository;
    ApplicationProperties applicationProperties;
    FileService fileService;
    AppointmentRepository appointmentRepository;
    private final UserFormService userFormService;

    public void generatePdfAppointmentReport(UUID idAppointment) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(idAppointment);
        Optional<AppointmentReport> appointmentReportOpt = appointmentReportRepository.findByAppointmentId(idAppointment);
        if (
            appointmentOpt.isPresent() &&
            appointmentReportOpt.isPresent() &&
            AppointmentStatusEnum.COMPLETED.equals(appointmentOpt.get().getStatus())
        ) {
            Appointment appointment = appointmentOpt.get();
            AppointmentReport appointmentReport = appointmentReportOpt.get();
            String fileName =
                String.join(
                    "_",
                    appointment.getTechnicalAdvisor().getUser().getNormalizedFullName(),
                    "AppointmentReport",
                    formatDate(appointment.getAppointmentDate(), DateTimeFormatter.ofPattern(DateTimeFormat.MM_DD_YYYY_DASH.getValue()))
                ) +
                ".pdf";
            try {
                PdfDocument pdf = createPdf(outputStream);
                Document document = createDocument(pdf);
                var portalOfBusinessOwner = appointment.getPortal();
                User businessOwner = appointment.getUser();
                generateHeaderAndFooter(
                    pdf,
                    appointment.getAppointmentDate(),
                    portalOfBusinessOwner,
                    businessOwner,
                    APPOINTMENT_REPORT_FOR,
                    "|"
                );
                generateAppointmentReportContent(document, appointment, appointmentReport);
                document.close();

                MultipartFile pdfFile = new ByteArrayMultipartFile(outputStream.toByteArray(), "file", fileName, "application/pdf");
                String pdfUrl = fileService.uploadFile(pdfFile, APPOINTMENT_REPORT_RESOURCE_KEY, false);
                appointmentReport.setPdfFilename(fileName);
                appointmentReport.setPdfUrl(pdfUrl);
                appointmentReportRepository.save(appointmentReport);
            } catch (Exception e) {
                log.error("Error generating PDF Appointment Report", e);
            }
        }
    }

    public void generatePdfProjectReport(UUID idProject) {
        Optional<Project> projectOpt = projectRepository.findById(idProject);
        Optional<ProjectReport> projectReportOpt = projectReportRepository.findByProjectId(idProject);
        if (projectOpt.isPresent() && projectReportOpt.isPresent()) {
            ProjectReport projectReport = generatePdfProjectReport(projectOpt.get(), projectReportOpt.get());
            projectReportRepository.save(projectReport);
        }
    }

    public ProjectReport generatePdfProjectReport(Project project, ProjectReport projectReport) {
        if (ProjectStatusEnum.COMPLETED.equals(project.getStatus())) {
            HashMap<String, String> fileMap = processGeneratePdfProjectReport(project, projectReport);
            projectReport.setPdfFilename(fileMap.get(PDF_FILENAME));
            projectReport.setPdfUrl(fileMap.get(PDF_PATH));
        }
        return projectReport;
    }

    private HashMap<String, String> processGeneratePdfProjectReport(Project project, ProjectReport projectReport) {
        HashMap<String, String> returnFileMap = new HashMap<>();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        String fileName =
            String.join(
                "_",
                project.getTechnicalAdvisor().getUser().getNormalizedFullName(),
                "ProjectReport",
                formatDate(project.getCompletedDate(), DateTimeFormatter.ofPattern(DateTimeFormat.MM_DD_YYYY_DASH.getValue()))
            ) +
            ".pdf";
        try {
            PdfDocument pdf = createPdf(outputStream);
            Document document = createDocument(pdf);
            var portalOfBusinessOwner = project.getPortal();
            User businessOwner = project.getBusinessOwner().getUser();
            generateHeaderAndFooter(pdf, project.getCompletedDate(), portalOfBusinessOwner, businessOwner, PROJECT_REPORT_FOR);
            generateProjectReportContent(document, project, projectReport);

            document.close();

            MultipartFile pdfFile = new ByteArrayMultipartFile(outputStream.toByteArray(), "file", fileName, "application/pdf");
            String pdfUrl = fileService.uploadFile(pdfFile, PROJECT_REPORT_RESOURCE_KEY, false);
            returnFileMap.put(PDF_PATH, pdfUrl);
            returnFileMap.put(PDF_FILENAME, fileName);
        } catch (Exception e) {
            log.error("Error generating PDF Project Report", e);
        }
        return returnFileMap;
    }

    private PdfDocument createPdf(ByteArrayOutputStream outputStream) {
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdf = new PdfDocument(writer);
        pdf.setDefaultPageSize(PageSize.LETTER);
        return pdf;
    }

    private Document createDocument(PdfDocument pdf) throws IOException {
        Document document = new Document(pdf);
        document.setFont(PdfFontFactory.createFont());
        document.setFontSize(FONT_SIZE);
        document.setMargins(MARGIN + (MARGIN / 2) + LOGO_HEIGHT, MARGIN, MARGIN + PADDING, MARGIN);
        return document;
    }

    private void generateHeaderAndFooter(PdfDocument pdf, Instant date, Portal portal, User user, String type) {
        generateHeaderAndFooter(pdf, date, portal, user, type, " ");
    }

    private void generateHeaderAndFooter(PdfDocument pdf, Instant date, Portal portal, User user, String type, String determineString) {
        String originalRole = getUserOriginalRole(user);
        String businessName = getBusinessNameIfApplicable(user, originalRole);
        pdf.addEventHandler(
            PdfDocumentEvent.END_PAGE,
            new HeaderFooterEventHandler(
                String.join(determineString, formatDate(date), portal.getPlatformName()),
                String.join(" ", type, businessName),
                getPortalUrl(portal)
            )
        );
    }

    private String getUserOriginalRole(User user) {
        return user
            .getAuthorities()
            .stream()
            .findFirst()
            .map(Authority::getName)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Role")));
    }

    private String getBusinessNameIfApplicable(User user, String originalRole) {
        if (RoleEnum.ROLE_BUSINESS_OWNER.getValue().equals(originalRole)) {
            var answerBusinessInfoMap = userFormService.getAnswerUserByQuestionCode(
                user.getId(),
                List.of(FormConstant.PORTAL_INTAKE_QUESTION_BUSINESS)
            );
            return Optional.ofNullable(answerBusinessInfoMap.get(FormConstant.PORTAL_INTAKE_QUESTION_BUSINESS)).orElse(StringUtils.EMPTY);
        }
        return StringUtils.EMPTY;
    }

    private void generateAppointmentReportContent(Document document, Appointment appointment, AppointmentReport appointmentReport) {
        AppointmentDetail appointmentDetail = appointment.getAppointmentDetail();
        Optional<Project> additionalScopeRequestOpt = projectRepository.findByAppointmentId(appointment.getId());
        boolean isAdditionalWork =
            additionalScopeRequestOpt.isPresent() && Boolean.TRUE.equals(additionalScopeRequestOpt.get().getIsAdditionalWork());
        // Add appointment session details table
        document.add(generateAppointmentSessionDetailsTable(appointment, appointmentReport, isAdditionalWork));
        document.add(new Paragraph(""));
        // Add content paragraphs
        document.add(new Paragraph("Initial Appointment Request:"));
        document.add(new Paragraph(appointmentDetail.getSupportDescription()));
        document.add(new Paragraph(appointmentDetail.getShareLinks()));
        document.add(
            new Paragraph(String.join(" ", "Outcomes:", String.join(",", getServiceOutcomesNames(appointmentDetail.getServiceOutcomes()))))
        );
        document.add(generateAppointmentSupportSummaryTable(appointmentReport));
        // Add additional scope table if applicable
        if (isAdditionalWork) {
            document.add(new Paragraph(""));
            document.add(new Paragraph(""));
            document.add(generateAppointmentScopeTable(additionalScopeRequestOpt.get()));
        }
    }

    private void generateProjectReportContent(Document document, Project project, ProjectReport projectReport) {
        document.add(generateProjectSessionDetailsTable(project, projectReport));
        document.add(new Paragraph(""));
        document.add(new Paragraph(""));
        document.add(generateProjectInitialTable(project));
        document.add(new Paragraph(""));
        document.add(new Paragraph(""));
        document.add(generateProjectSupportSummaryTable(projectReport));
    }

    private String getAppointmentDate(Appointment appointment) {
        ZonedDateTime appointmentZoneDateAndTime = DateUtils.combineDateTimeWithZone(
            appointment.getAppointmentDate(),
            appointment.getTimezone()
        );
        return appointmentZoneDateAndTime.format(DateTimeFormatter.ofPattern(DateTimeFormat.MM_DD_YYYY.getValue()));
    }

    private String formatDate(Instant instant) {
        return formatDate(instant, "");
    }

    private String formatDate(Instant instant, DateTimeFormatter formatter) {
        return formatDate(instant, formatter, "");
    }

    private String formatDate(Instant instant, String isNullMessage) {
        return formatDate(instant, DateTimeFormatter.ofPattern(DateTimeFormat.MM_DD_YYYY.getValue()), isNullMessage);
    }

    private String formatDate(Instant instant, DateTimeFormatter formatter, String isNullMessage) {
        if (Objects.isNull(instant)) {
            return isNullMessage;
        }
        return instant.atZone(ZoneId.of(TIMEZONE)).toLocalDate().format(formatter);
    }

    private String getCategoryName(UUID categoryId) {
        return categoryRepository.findById(categoryId).map(Category::getName).orElse("");
    }

    private String createAdvisorLine(User user) {
        return "Advisor: " + user.getNormalizedFullName();
    }

    private String getServiceOutcomesNames(String serviceOutcomeIds) {
        if (Objects.isNull(serviceOutcomeIds)) return "No data";
        List<ServiceOutcome> serviceOutcomes = serviceOutcomeRepository.findAllById(
            Arrays.stream(serviceOutcomeIds.split(",")).map(UUID::fromString).toList()
        );
        List<String> serviceOutcomesName = serviceOutcomes.stream().map(ServiceOutcome::getName).toList();
        return String.join(", ", serviceOutcomesName);
    }

    private Table generateAppointmentSessionDetailsTable(
        Appointment appointment,
        AppointmentReport appointmentReport,
        boolean isAdditionalScopeRequested
    ) {
        User businessOwner = appointment.getUser();
        String originalRole = getUserOriginalRole(businessOwner);
        String businessName = getBusinessNameIfApplicable(businessOwner, originalRole);
        String businessOwnerLine = String.join(
            " ",
            "Business Owner: ",
            String.join(", ", businessName, businessOwner.getNormalizedFullName(), businessOwner.getEmail())
        );
        String advisorLine = createAdvisorLine(appointment.getTechnicalAdvisor().getUser());
        String categoryLine = String.join(" ", "Category:", getCategoryName(appointment.getAppointmentDetail().getCategoryId()));
        Table table = new Table(UnitValue.createPercentArray(new float[] { 2, 2 })).useAllAvailableWidth();
        table.addCell(createHeaderCell("SESSION DETAILS"));
        table.addCell(createHeaderCell(String.join(" ", "Date & Time:", getAppointmentDate(appointment), "60 Minutes")));
        table.addCell(createContentCell(String.join("\n", businessOwnerLine, advisorLine, categoryLine)));

        table.addCell(
            createContentCell(
                String.join(
                    "\n",
                    "Provider: HUUB",
                    String.join(" ", "Appointment Date:", getAppointmentDate(appointment)),
                    String.join(" ", "Report Submission Date:", formatDate(appointmentReport.getCreatedDate())),
                    String.join(" ", "Additional Scope requested (Y/N):", isAdditionalScopeRequested ? "Yes" : "No")
                )
            )
        );
        return table;
    }

    private Table generateAppointmentSupportSummaryTable(AppointmentReport appointmentReport) {
        List<EntityAttachment> appointmentReportAttachments = entityAttachmentRepository.findByEntityIdAndEntityType(
            appointmentReport.getId(),
            EntityTypeEnum.APPOINTMENT_REPORT
        );
        List<String> attachmentsName = appointmentReportAttachments.stream().map(EntityAttachment::getName).toList();
        Table table = new Table(UnitValue.createPercentArray(new float[] { 3, 1 })).useAllAvailableWidth();
        table.addCell(createHeaderCell("SUPPORT SUMMARY"));
        table.addCell(createHeaderCell("OUTCOMES:"));
        table.addCell(
            createContentCell(
                String.join(
                    "\n\n",
                    String.join(
                        " ",
                        "Accounting data, tax planning -",
                        (Objects.nonNull(appointmentReport.getDescription()) ? appointmentReport.getDescription() : "No data")
                    ),
                    String.join(" ", "Attachments -", (!attachmentsName.isEmpty() ? String.join(",\n", attachmentsName) : "No data")),
                    String.join(
                        " ",
                        "Additional Comments -",
                        (Objects.nonNull(appointmentReport.getFeedback()) ? appointmentReport.getFeedback() : "No data")
                    )
                )
            )
        );
        table.addCell(createContentCell(String.join(",\n", getServiceOutcomesNames(appointmentReport.getServiceOutcomes()))));
        return table;
    }

    private Table generateAppointmentScopeTable(Project project) {
        Table table = new Table(UnitValue.createPercentArray(new float[] { 2, 1 })).useAllAvailableWidth();
        table.addCell(createHeaderCell("ADDITIONAL SCOPE REQUEST"));
        table.addCell(createHeaderCell(""));
        table.addCell(createContentCell(project.getScopeOfWork()));
        table.addCell(
            createContentCell(
                String.join(
                    "\n",
                    String.join(" ", "Requested Hours:", String.valueOf(Integer.max(Math.round(project.getEstimatedHoursNeeded()), 0))),
                    String.join(" ", "Est. Completion Date:", formatDate(project.getEstimatedCompletionDate()))
                )
            )
        );
        return table;
    }

    private Table generateProjectSessionDetailsTable(Project project, ProjectReport projectReport) {
        User businessOwner = project.getBusinessOwner().getUser();
        String originalRole = getUserOriginalRole(businessOwner);
        String businessName = getBusinessNameIfApplicable(businessOwner, originalRole);
        String businessOwnerLine = String.join(
            " ",
            "Business Owner: ",
            String.join(", ", businessName, businessOwner.getNormalizedFullName(), businessOwner.getEmail())
        );
        String advisorLine = createAdvisorLine(project.getTechnicalAdvisor().getUser());
        String categoryLine = String.join(" ", "Category:", getCategoryName(project.getCategoryId()));
        Table table = new Table(UnitValue.createPercentArray(new float[] { 2, 2 })).useAllAvailableWidth();
        table.addCell(createContentCell("SESSION DETAILS"));
        table.addCell(
            createContentCell(
                String.join(
                    " ",
                    "Completion Date:",
                    formatDate(project.getCompletedDate()),
                    String.valueOf(Integer.max(projectReport.getHoursCompleted(), 0))
                )
            )
        );
        table.addCell(createContentCell(String.join("\n", businessOwnerLine, advisorLine, categoryLine)));

        table.addCell(
            createContentCell(
                String.join(
                    "\n",
                    "Provider: HUUB",
                    "Status: Completed",
                    String.join(" ", "Proposed Start Date:", formatDate(project.getProposedStartDate(), "Not Specified")),
                    String.join(" ", "Est. Completion Date:", formatDate(project.getEstimatedCompletionDate())),
                    String.join(" ", "Est. Hours:", String.valueOf(Integer.max(Math.round(project.getEstimatedHoursNeeded()), 0))),
                    String.join(" ", "Actual Hours:", String.valueOf(Integer.max(projectReport.getHoursCompleted(), 0))),
                    String.join(" ", "Additional Scope requested (Y/N):", project.getIsAdditionalWork() ? "Yes" : "No")
                )
            )
        );
        return table;
    }

    private Table generateProjectSupportSummaryTable(ProjectReport projectReport) {
        Table table = new Table(UnitValue.createPercentArray(new float[] { 4, 1 })).useAllAvailableWidth();
        table.addCell(createContentCell("SUPPORT SUMMARY"));
        table.addCell(createContentCell("OUTCOMES:"));
        table.addCell(createContentCell(String.join("\n", projectReport.getDescription(), "\n", "COMMENTS", projectReport.getFeedback())));
        table.addCell(createContentCell(String.join(", ", getServiceOutcomesNames(projectReport.getServiceOutcomes()))));
        return table;
    }

    private Table generateProjectInitialTable(Project project) {
        List<ServiceOutcome> serviceOutcomes = serviceOutcomeRepository.getAllByServiceOfferedId(project.getServiceId());
        List<String> serviceOutcomesName = serviceOutcomes.stream().map(ServiceOutcome::getName).toList();
        Table table = new Table(UnitValue.createPercentArray(new float[] { 3, 1 })).useAllAvailableWidth();
        table.addCell(createContentCell("INITIAL PROJECT SCOPE "));
        table.addCell(createContentCell("OUTCOMES:"));
        table.addCell(createContentCell(project.getScopeOfWork()));
        table.addCell(createContentCell(String.join(", ", serviceOutcomesName)));
        return table;
    }

    private String getPortalUrl(Portal portal) {
        try {
            String portalUrl;
            if (Objects.isNull(portal.getUrl())) {
                URI uri = new URI(applicationProperties.getClientApp().getBaseUrl());
                URL urlObject = uri.toURL();
                portalUrl = urlObject.getHost() + urlObject.getPath();
            } else if (Boolean.TRUE.equals(portal.getIsCustomDomain())) {
                URI uri = new URI(portal.getUrl());
                URL urlObject = uri.toURL();
                portalUrl = urlObject.getHost() + urlObject.getPath();
            } else {
                portalUrl = String.join("/", CLIENT_APP_BASE_URL, portal.getUrl());
            }
            return portalUrl;
        } catch (URISyntaxException | MalformedURLException e) {
            return "";
        }
    }

    private Cell createHeaderCell(String text) {
        return new Cell().add(new Paragraph(text).setBold()).setBackgroundColor(LIGHT_GRAY).setPadding(PADDING).setBorder(BORDER);
    }

    private Cell createContentCell(String text) {
        return new Cell().add(new Paragraph(text)).setPadding(PADDING).setBorder(BORDER);
    }

    private record HeaderFooterEventHandler(String headerText, String headerTextTitle, String footerText) implements IEventHandler {
        @Override
        public void handleEvent(Event event) {
            try {
                PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
                PdfDocument pdfDoc = docEvent.getDocument();
                PdfPage page = docEvent.getPage();
                PdfCanvas canvas = new PdfCanvas(page);
                Document doc = new Document(pdfDoc);
                Resource resourceLogo = new ClassPathResource(LOGO_PATH);
                ImageData imageData = ImageDataFactory.create(resourceLogo.getFile().getAbsolutePath());
                Image logo = new Image(imageData).scaleToFit(LOGO_WIDTH, LOGO_HEIGHT);
                float logoY = pdfDoc.getDefaultPageSize().getTop() - (MARGIN / 2);
                logo.setFixedPosition(MARGIN, logoY - LOGO_HEIGHT);
                doc.add(logo);
                float textX = MARGIN + LOGO_WIDTH + (PADDING / 2);
                float textY = logoY - PADDING;
                PdfFont font = PdfFontFactory.createFont();

                List<String> lines = wrapText(headerTextTitle, font, WRAP_HEADER_WIDTH, FONT_SIZE);
                List<String> listLine = lines.subList(0, Math.min(2, lines.size()));
                canvas.beginText().setFontAndSize(font, FONT_SIZE).moveText(textX, textY).showText(headerText);
                for (int i = 0; i < 2 && i < listLine.size(); i++) {
                    canvas
                        .moveText(0, -((PADDING / 2) + FONT_SIZE))
                        .showText(listLine.get(i) + (i == (listLine.size() - 1) && listLine.size() < lines.size() ? "..." : ""));
                }
                canvas.endText();

                float footerY = pdfDoc.getDefaultPageSize().getBottom() + MARGIN;
                float footerX = pdfDoc.getDefaultPageSize().getWidth() / 2;
                doc.showTextAligned(
                    new Paragraph(String.join("  |  ", footerText, FOOTER_SOLOGAN)).setFontSize(8),
                    footerX,
                    footerY,
                    pdfDoc.getPageNumber(page),
                    TextAlignment.CENTER,
                    VerticalAlignment.BOTTOM,
                    0
                );
            } catch (IOException e) {
                log.error("Error handling header/footer", e);
            }
        }

        private List<String> wrapText(String text, PdfFont font, float maxWidth, float fontSize) {
            List<String> lines = new ArrayList<>();
            String[] words = text.split(" ");
            StringBuilder currentLine = new StringBuilder();
            for (String word : words) {
                currentLine.append(word).append(" ");
                if (getTextWidth(currentLine.toString(), font, fontSize) > maxWidth) {
                    // If the current line exceeds max width, add the previous line and start a new one
                    lines.add(currentLine.toString().trim());
                    currentLine = new StringBuilder(word + " ");
                }
            }
            // Add the last line
            if (!currentLine.isEmpty()) {
                lines.add(currentLine.toString().trim());
            }
            return lines;
        }

        private float getTextWidth(String text, PdfFont font, float fontSize) {
            return (font.getWidth(text) * fontSize) / 1000; // Scale the width by the font size
        }
    }
}
