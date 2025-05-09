package com.formos.huub.web.rest.v2;

import com.formos.huub.framework.base.BaseController;
import com.formos.huub.framework.enums.DateTimeFormat;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.repository.AppointmentRepository;
import com.formos.huub.repository.UserRepository;
import com.formos.huub.service.pdf.InvoicePdfService;
import com.formos.huub.service.pdf.PdfService;
import com.formos.huub.service.pushnotification.PushNotificationService;
import com.formos.huub.service.schedule.*;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/tests")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TestController extends BaseController {

    private final ResponseSupport responseSupport;
    PasswordEncoder passwordEncoder;
    ProjectReportScheduleService projectReportScheduleService;
    ProjectScheduleService projectScheduleService;
    AppointmentScheduleService appointmentScheduleService;
    MonthlyInvoiceScheduleService invoiceScheduleService;
    PdfService pdfService;
    InvoicePdfService invoicePdfService;
    PortalScheduleService portalScheduleService;
    private final PushNotificationService pushNotificationService;
    private final AppointmentRepository appointmentRepository;
    //    private final IMailService mailService;
    private final UserRepository userRepository;

    @GetMapping("")
    @ResponseBody
    public ResponseEntity<ResponseData> testResponse() {
        String password = "Rn1#s&2AFB9RWCtV";
        String encryptedPassword = passwordEncoder.encode(password);
        //        throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "User"));
        return responseSupport.success(ResponseData.builder().data(encryptedPassword).build());
    }

    @GetMapping("/send-mail-reminder-400")
    public ResponseEntity<ResponseData> testSendMail() {
        log.info("Test send mail reminder");
        projectReportScheduleService.sendMailReminder();
        return responseSupport.success(ResponseData.builder().data("Call API send mail reminder").build());
    }

    @GetMapping("/send-mail-reminder-389")
    public ResponseEntity<ResponseData> jobScheduleReminderNotApprovedProject() {
        projectScheduleService.jobScheduleReminderNotApprovedProject();
        return responseSupport.success();
    }

    @GetMapping("/send-project-391")
    public ResponseEntity<ResponseData> jobScheduleAutoDenialProject() {
        projectScheduleService.jobScheduleAutoDenialProject();
        return responseSupport.success();
    }

    @GetMapping("/send-project-b845")
    public ResponseEntity<ResponseData> jobScheduleAutoOverdueProject() {
        projectScheduleService.jobScheduleAutoOverdueProject();
        return responseSupport.success();
    }

    @GetMapping("/send-mail-appointment-387")
    public ResponseEntity<ResponseData> jobScheduleReminderPreAppointment() {
        appointmentScheduleService.jobScheduleReminderPreAppointment();
        return responseSupport.success();
    }

    @GetMapping("/create-invoice")
    public ResponseEntity<ResponseData> createInvoice(@RequestParam String previousMonth) {
        YearMonth yearMonth = YearMonth.parse(previousMonth, DateTimeFormatter.ofPattern(DateTimeFormat.YYYY_MM.getValue()));
        invoiceScheduleService.generateMonthlyInvoices(yearMonth);
        return responseSupport.success();
    }

    @GetMapping("/export-pdf-invoice/{id}")
    public ResponseEntity<ResponseData> exportPdfInvoice(@PathVariable String id) {
        invoicePdfService.generateInvoicePdf(UUID.fromString(id));
        return responseSupport.success();
    }

    @GetMapping("/pdf-a-449/{id}")
    public ResponseEntity<ResponseData> generatePdfAppointmentReport(@PathVariable String id) {
        pdfService.generatePdfAppointmentReport(UUID.fromString(id));
        return responseSupport.success(ResponseData.builder().build());
    }

    @GetMapping("/pdf-p-449/{id}")
    public ResponseEntity<ResponseData> generatePdfProjectReport(@PathVariable String id) {
        pdfService.generatePdfProjectReport(UUID.fromString(id));
        return responseSupport.success(ResponseData.builder().build());
    }

    //    @GetMapping("/email-html/{id}")
    //    public ResponseEntity<ResponseData> viewMailHtml(@PathVariable String id) {
    //        var user = userRepository.findById(UUID.fromString(id)).get();
    //        mailService.sendActivationEmail(user);
    //        return responseSupport.success(ResponseData.builder().build());
    //    }

    @GetMapping("/appointment-392")
    public ResponseEntity<ResponseData> updateAppointmentStatuses() {
        portalScheduleService.updateAppointmentStatuses();
        return responseSupport.success(ResponseData.builder().build());
    }

    @GetMapping("/mail-appointment-392/{id}")
    @Transactional
    public ResponseEntity<ResponseData> sendMailAppointment(@PathVariable String id) {
        var appointment = appointmentRepository.findById(UUID.fromString(id)).get();
        pushNotificationService.sendPostAppointmentMailForBusinessOwner(appointment);
        pushNotificationService.sendPostAppointmentReportReminderMailForAdvisor(appointment, 1);
        pushNotificationService.sendPostAppointmentReportReminderMailForAdvisor(appointment, 3);
        pushNotificationService.sendPostAppointmentReportReminderMailForAdvisor(appointment, 7);
        pushNotificationService.sendPostAppointmentReportReminderMailForAdvisor(appointment, 14);
        pushNotificationService.sendPostAppointmentReportReminderMailForAdvisor(appointment, 30);
        return responseSupport.success(ResponseData.builder().build());
    }
}
