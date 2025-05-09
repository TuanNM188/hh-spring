package com.formos.huub.service.appointmentreport;

import com.formos.huub.domain.entity.AppointmentReport;
import com.formos.huub.domain.enums.AppointmentStatusEnum;
import com.formos.huub.domain.request.appointmentreport.RequestCreateAppointmentReport;
import com.formos.huub.domain.request.appointmentreport.RequestSubmitAppointmentReport;
import com.formos.huub.domain.response.appointmentreport.ResponseAppointmentReport;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.utils.UUIDUtils;
import com.formos.huub.mapper.appointmentreport.AppointmentReportMapper;
import com.formos.huub.repository.AppointmentReportRepository;
import com.formos.huub.repository.AppointmentRepository;
import com.formos.huub.service.common.SequenceService;
import com.formos.huub.service.entityattachment.EntityAttachmentService;
import com.formos.huub.service.pdf.PdfService;
import jakarta.validation.Valid;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppointmentReportService {

    AppointmentReportRepository appointmentReportRepository;

    PdfService pdfService;

    AppointmentRepository appointmentRepository;

    AppointmentReportMapper appointmentReportMapper;

    EntityAttachmentService entityAttachmentService;

    SequenceService sequenceGeneratorService;

    private static final Integer MAX_SECTION_FILES = 8;
    private static final String APPOINTMENT_REPORT_SEQUENCE = "appointment_report_sequence";

    /**
     * create Appointment Report
     *
     * @param request RequestCreateAppointmentReport
     */
    public UUID createAppointmentReport(@Valid RequestCreateAppointmentReport request) {
        // Validate input constraints
        validDateRequestAppointmentReport(request.getAttachments().size(), request.getServiceOutcomes().size());

        // Convert appointment ID and map request to entity
        var appointmentId = UUIDUtils.toUUID(request.getAppointmentId());
        var appointmentReport = appointmentReportMapper.toEntity(request, appointmentId);
        Long reportNumber = sequenceGeneratorService.getNextSequenceValue(APPOINTMENT_REPORT_SEQUENCE);
        appointmentReport.setReportNumber(reportNumber.toString());
        // Save the main appointment report
        appointmentReportRepository.save(appointmentReport);

        // Handle attachments if present
        entityAttachmentService.saveAllAttachment(request.getAttachments(), appointmentReport.getId());

        return appointmentReport.getId();
    }

    public ResponseAppointmentReport submitAppointmentReport(@Valid RequestSubmitAppointmentReport request) {
        if (Boolean.TRUE.equals(request.getBusinessOwnerAttended())) {
            validDateRequestAppointmentReport(request.getAttachments().size(), request.getServiceOutcomes().size());
        }

        var appointmentId = UUIDUtils.toUUID(request.getAppointmentId());
        var appointmentReport = appointmentReportMapper.toEntity(request, appointmentId);
        Long reportNumber = sequenceGeneratorService.getNextSequenceValue(APPOINTMENT_REPORT_SEQUENCE);
        appointmentReport.setReportNumber(reportNumber.toString());
        appointmentReportRepository.save(appointmentReport);
        entityAttachmentService.saveAllAttachment(request.getAttachments(), appointmentReport.getId());
        return buildResponseAppointmentReport(appointmentReport.getId(), appointmentId);
    }

    private ResponseAppointmentReport buildResponseAppointmentReport(UUID reportId, UUID appointmentId) {
        ResponseAppointmentReport response = new ResponseAppointmentReport();
        response.setAppointmentReportId(reportId);

        appointmentRepository
            .findById(appointmentId)
            .ifPresent(appointment -> {
                appointment.setStatus(AppointmentStatusEnum.COMPLETED);
                appointmentRepository.save(appointment);
                pdfService.generatePdfAppointmentReport(appointment.getId());
                Optional.ofNullable(appointment.getTechnicalAssistanceSubmit()).ifPresent(technicalAssistance -> {
                    response.setRemainingHours(technicalAssistance.getRemainingAwardHours());
                    if (Objects.nonNull(technicalAssistance.getProgramTerm())) {
                        response.setProgramTermStartDate(technicalAssistance.getProgramTerm().getStartDate());
                        response.setProgramTermEndDate(technicalAssistance.getProgramTerm().getEndDate());
                    }
                });
            });
        return response;
    }

    private void validDateRequestAppointmentReport(Integer numberOfFiles, Integer numberOfServiceOutcomes) {
        validateMaxFiles(numberOfFiles, MAX_SECTION_FILES, "Num of Files");
        validateRequireServiceOutcomes(numberOfServiceOutcomes, "Service Outcomes");
    }

    /**
     * Validate the maximum number of files
     *
     * @param currentNumberOfFiles current number of files request
     * @param maxFiles             max files
     * @param fieldName            field name
     */
    public static void validateMaxFiles(Integer currentNumberOfFiles, Integer maxFiles, String fieldName) {
        if (currentNumberOfFiles > maxFiles) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0038, fieldName, maxFiles));
        }
    }

    /**
     * @param currentSize current size of list service outcomes
     */
    private void validateRequireServiceOutcomes(Integer currentSize, String fieldName) {
        if (currentSize <= 0) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0027, fieldName));
        }
    }
}
