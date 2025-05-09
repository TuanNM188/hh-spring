/**
 * ***************************************************
 * * Description :
 * * File        : InvoiceScheduleService
 * * Author      : Hung Tran
 * * Date        : Mar 03, 2025
 * ***************************************************
 **/
package com.formos.huub.service.schedule;

import com.formos.huub.domain.entity.*;
import com.formos.huub.domain.enums.AppointmentStatusEnum;
import com.formos.huub.domain.enums.ProjectStatusEnum;
import com.formos.huub.framework.enums.DateTimeFormat;
import com.formos.huub.repository.*;
import com.formos.huub.service.common.SequenceService;
import com.formos.huub.service.pdf.InvoicePdfService;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MonthlyInvoiceScheduleService {

    private final ProjectRepository projectRepository;
    private final AppointmentRepository appointmentRepository;
    private final ProgramTermVendorRepository programTermVendorRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceDetailRepository invoiceDetailRepository;
    private final SequenceService sequenceGeneratorService;
    private final InvoicePdfService invoicePdfService;

    private static final String INVOICE_SEQUENCE = "invoice_sequence";
    private static final String APPOINTMENT_DESCRIPTION = "%s\nAppointment - %d Hour(s)\nFor the period of %s";
    private static final String PROJECT_DESCRIPTION = "%s\nProject - %d Hour(s)\nFor the period of %s";

    DateTimeFormatter formatter_YYYY_MM = DateTimeFormatter.ofPattern(DateTimeFormat.YYYY_MM.getValue());
    DateTimeFormatter formatter_MM_DD_YY = DateTimeFormatter.ofPattern(DateTimeFormat.MM_DD_YY.getValue());

    @Value("${schedule.timezone}")
    private String timeZoneId;

    private ZoneId configuredZone;

    @PostConstruct
    public void init() {
        this.configuredZone = ZoneId.of(timeZoneId);
    }

    /**
     * Generate monthly invoices for all technical advisors with activity in the previous month.
     * This is the main entry point for the invoice generation process.
     */
    public void generateMonthlyInvoices(YearMonth previousMonth) {
        try {
            // Determine the billing period (previous month)
            LocalDateTime monthStart = previousMonth.atDay(1).atStartOfDay();
            LocalDateTime monthEnd = previousMonth.atEndOfMonth().plusDays(1).atStartOfDay().minusSeconds(1);

            Instant startInstant = monthStart.atZone(this.configuredZone).toInstant();
            Instant endInstant = monthEnd.atZone(this.configuredZone).toInstant();

            log.info("Generating invoices for period: {} to {}", startInstant, endInstant);

            // Process invoices by technical advisor and portal
            processInvoicesByAdvisor(startInstant, endInstant, previousMonth);

            log.info("Completed monthly invoice generation job");
        } catch (Exception e) {
            log.error("Error in invoice generation job", e);
            throw new RuntimeException("Invoice generation failed", e);
        }
    }

    /**
     * Process invoices by finding active technical advisors and their portals.
     */
    private void processInvoicesByAdvisor(Instant startDate, Instant endDate, YearMonth invoiceMonth) {
        // Find active technical advisors and process each one
        Map<TechnicalAdvisor, Set<Portal>> advisorPortalMap = findActiveAdvisorsWithPortals(startDate, endDate);

        log.info("Found {} active technical advisors with activity in the specified period", advisorPortalMap.size());

        advisorPortalMap.forEach((advisor, portals) -> {
            try {
                log.info("Processing advisor: {} with {} portals", advisor.getId(), portals.size());
                portals.forEach(portal -> createInvoiceForAdvisorAndPortal(advisor, portal, startDate, endDate, invoiceMonth));
            } catch (Exception e) {
                log.error("Error processing advisor: {}", advisor.getId(), e);
            }
        });
    }

    /**
     * Find all technical advisors with activity in the specified period and their associated portals.
     * Returns a map of technical advisor to set of portals where they have activity.
     */
    private Map<TechnicalAdvisor, Set<Portal>> findActiveAdvisorsWithPortals(Instant startDate, Instant endDate) {
        Map<TechnicalAdvisor, Set<Portal>> result = new HashMap<>();

        // Find all completed appointments and extract advisor and portal information
        List<Appointment> appointments = appointmentRepository.findByStatusAndAppointmentDateBetween(
            AppointmentStatusEnum.COMPLETED,
            startDate,
            endDate
        );

        appointments
            .stream()
            .filter(a -> a.getTechnicalAdvisor() != null && a.getPortal() != null)
            .forEach(a -> {
                TechnicalAdvisor advisor = a.getTechnicalAdvisor();
                Portal portal = a.getPortal();
                result.computeIfAbsent(advisor, k -> new HashSet<>()).add(portal);
            });

        // Find all completed projects and extract advisor and portal information

        projectRepository.findByStatusAndCompletedDateBetween(ProjectStatusEnum.COMPLETED, startDate, endDate)
            .stream()
            .filter(p -> p.getTechnicalAdvisor() != null && p.getPortal() != null)
            .forEach(p -> {
                TechnicalAdvisor advisor = p.getTechnicalAdvisor();
                Portal portal = p.getPortal();
                result.computeIfAbsent(advisor, k -> new HashSet<>()).add(portal);
            });

        return result;
    }

    /**
     * Create an invoice for a specific technical advisor and portal.
     */
    private void createInvoiceForAdvisorAndPortal(
        TechnicalAdvisor advisor,
        Portal portal,
        Instant startDate,
        Instant endDate,
        YearMonth invoiceMonth
    ) {
        // First check if the invoice already exists
        String monthYearStr = invoiceMonth.format(this.formatter_YYYY_MM);
        List<Invoice> existingInvoices = invoiceRepository.findByTechnicalAdvisorIdAndPortalIdAndInvoiceMonth(
            advisor.getId(),
            portal.getId(),
            monthYearStr
        );

        // Use existing invoice or create a new one
        Invoice invoice;

        if (!existingInvoices.isEmpty()) {
            invoice = existingInvoices.getFirst();
            invoiceDetailRepository.deleteAllByInvoiceId(invoice.getId());
            log.info("Using existing invoice {} for advisor: {}, portal: {}", invoice.getInvoiceNumber(), advisor.getId(), portal.getId());
        } else {
            invoice = createNewInvoice(advisor, portal, invoiceMonth);
            invoice = invoiceRepository.save(invoice);
            log.info("Created new invoice {} for advisor: {}, portal: {}", invoice.getInvoiceNumber(), advisor.getId(), portal.getId());
        }

        // Find all completed appointments and projects for this advisor and portal
        List<Appointment> appointments = appointmentRepository.findByTechnicalAdvisorAndPortalAndStatusAndAppointmentDateBetween(
            advisor,
            portal,
            AppointmentStatusEnum.COMPLETED,
            startDate,
            endDate
        );

        List<Project> projects = projectRepository.findByTechnicalAdvisorAndPortalAndStatusAndCompletedDateBetween(
            advisor,
            portal,
            ProjectStatusEnum.COMPLETED,
            startDate,
            endDate
        );

        var programTermVendors = programTermVendorRepository.findAllByVendorIdAndDate(advisor.getCommunityPartner().getId(), portal.getId(), startDate, endDate);
        if (programTermVendors.isEmpty()) {
            return;
        }

        BigDecimal contractedRate = programTermVendors.getFirst().getContractedRate();
        if (contractedRate == null) {
            log.info("No Contract Rate found for advisor: {} and portal: {}", advisor.getId(), portal.getId());
            return;
        }

        // Process each submit and create invoice details
        double totalAmount = processSubmitsAndCreateDetails(
            invoice,
            contractedRate,
            appointments,
            projects,
            startDate,
            endDate
        );

        // Update invoice total amount
        invoice.setTotalAmount(totalAmount);
        invoiceRepository.save(invoice);

        // Update status of all appointments and projects to INVOICED
        updateItemsToInvoiced(appointments, projects, invoice.getId());

        log.info(
            "Finalized invoice {} for advisor: {}, portal: {}, total amount: {}",
            invoice.getInvoiceNumber(),
            advisor.getId(),
            portal.getId(),
            totalAmount
        );
        invoicePdfService.generateInvoicePdf(invoice);
    }

    /**
     * Process all submits for an invoice and create the invoice details.
     * Returns the total amount for the invoice.
     */
    private double processSubmitsAndCreateDetails(
        Invoice invoice,
        BigDecimal contractedRate,
        List<Appointment> appointments,
        List<Project> projects,
        Instant startDate,
        Instant endDate
    ) {
        double totalAmount = 0.0;

        // Create appointment invoice detail
        InvoiceDetail appointmentInvoiceDetail = createInvoiceDetail(
            invoice,
            appointments.size(),
            APPOINTMENT_DESCRIPTION,
            contractedRate,
            startDate,
            endDate,
            true
        );
        if (appointmentInvoiceDetail != null) {
            log.info("Created Appointment invoice detail with {} hours, amount: {}",
                appointmentInvoiceDetail.getTotalHours(),
                appointmentInvoiceDetail.getAmount()
            );
            totalAmount += appointmentInvoiceDetail.getAmount();
        }

        // Create project invoice detail
        InvoiceDetail projectInvoiceDetail = createInvoiceDetail(
            invoice,
            calculateProjectHours(projects),
            PROJECT_DESCRIPTION,
            contractedRate,
            startDate,
            endDate,
            false
        );
        if (projectInvoiceDetail != null) {
            log.info("Created Project invoice detail with {} hours, amount: {}",
                projectInvoiceDetail.getTotalHours(),
                projectInvoiceDetail.getAmount()
            );
            totalAmount += projectInvoiceDetail.getAmount();
        }

        return totalAmount;
    }

    /**
     * Create an invoice detail for a specific TechnicalAssistanceSubmit.
     */
    private InvoiceDetail createInvoiceDetail(
        Invoice invoice,
        int totalHours,
        String descriptionFormat,
        BigDecimal rate,
        Instant startDate,
        Instant endDate,
        Boolean isAppointment
    ) {

        // If there's no activity, don't create a detail
        if (totalHours == 0) {
            return null;
        }

        // Format period for description
        String period = formatPeriodForDisplay(startDate, endDate);

        // Build description
        String description = String.format(descriptionFormat, invoice.getPayToName(), totalHours, period);

        // Calculate total amount
        double amount = BigDecimal.valueOf(totalHours).multiply(rate).doubleValue();

        // Create invoice detail
        InvoiceDetail detail = InvoiceDetail.builder()
            .invoice(invoice)
            .itemName("Advisor Hours")
            .description(description)
            .totalHours(totalHours)
            .totalAppointmentHours(totalHours)
            .totalProjectHours(totalHours)
            .price(rate.doubleValue())
            .amount(amount)
            .servicePeriodStart(startDate)
            .servicePeriodEnd(endDate)
            .isAppointment(isAppointment)
            .build();

        // Save and return the detail
        detail = invoiceDetailRepository.save(detail);

        return detail;
    }

    /**
     * Calculate total project hours from a list of projects.
     */
    private int calculateProjectHours(List<Project> projects) {
        return projects.stream()
            .filter(p -> p.getProjectReport() != null)
            .mapToInt(p -> p.getProjectReport().getHoursCompleted()).sum();
    }

    /**
     * Update appointments and projects to INVOICED status.
     */
    private void updateItemsToInvoiced(List<Appointment> appointments, List<Project> projects, UUID invoiceId) {
        if (!appointments.isEmpty()) {
            appointments.forEach(a -> {
                a.setStatus(AppointmentStatusEnum.INVOICED);
                a.setInvoiceId(invoiceId);
            });
            appointmentRepository.saveAll(appointments);
            log.info("Updated {} appointments to INVOICED status", appointments.size());
        }

        if (!projects.isEmpty()) {
            projects.forEach(p -> {
                p.setStatus(ProjectStatusEnum.INVOICED);
                p.setInvoiceId(invoiceId);
            });
            projectRepository.saveAll(projects);
            log.info("Updated {} projects to INVOICED status", projects.size());
        }
    }

    /**
     * Create a new invoice for a technical advisor and portal.
     */
    private Invoice createNewInvoice(TechnicalAdvisor technicalAdvisor, Portal portal, YearMonth invoiceMonth) {
        // Get next invoice number
        Long invoiceNumber = sequenceGeneratorService.getNextSequenceValue(INVOICE_SEQUENCE);

        // Convert invoiceMonth to due date (last day of month)
        Instant dueDate = invoiceMonth.atEndOfMonth().atStartOfDay().toInstant(ZoneOffset.UTC);

        // Get user information for the technical advisor
        var technicalAdvisorUser = technicalAdvisor.getUser();

        // Create invoice using Builder pattern
        return Invoice.builder()
            .invoiceNumber(invoiceNumber.toString())
            .dueDate(dueDate)
            .invoiceMonth(invoiceMonth.format(this.formatter_YYYY_MM))
            // Portal info
            .portalId(portal.getId())
            .billToName(portal.getPlatformName())
            .billToAddress(portal.getAddress1())
            .billToCity(portal.getCity())
            .billToState(portal.getState())
            .billToZip(portal.getZipCode())
            .billToPhone(portal.getBillingPhone())
            // Technical advisor info
            .technicalAdvisorId(technicalAdvisor.getId())
            .payToName(technicalAdvisorUser.getNormalizedFullName())
            .payToAddress(technicalAdvisorUser.getAddress1())
            .payToCity(technicalAdvisorUser.getCity())
            .payToState(technicalAdvisorUser.getState())
            .payToZip(technicalAdvisorUser.getZipCode())
            .payToPhone(technicalAdvisorUser.getPhoneNumber())
            .totalAmount(0.0)
            .build();
    }

    /**
     * Format period for display (MM/DD/YY-MM/DD/YY).
     */
    private String formatPeriodForDisplay(Instant start, Instant end) {
        LocalDate startDate = LocalDateTime.ofInstant(start, this.configuredZone).toLocalDate();
        LocalDate endDate = LocalDateTime.ofInstant(end, this.configuredZone).toLocalDate();

        return startDate.format(this.formatter_MM_DD_YY) + "-" + endDate.format(this.formatter_MM_DD_YY);
    }
}
