package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.request.appointmentreport.RequestCreateAppointmentReport;
import com.formos.huub.domain.request.appointmentreport.RequestSubmitAppointmentReport;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.service.appointmentreport.AppointmentReportService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/appointment-report")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppointmentReportController {

    ResponseSupport responseSupport;

    AppointmentReportService appointmentReportService;

    @PreAuthorize("hasPermission(null, 'CREATE_APPOINTMENT_REPORT')")
    @PostMapping
    public ResponseEntity<ResponseData> create(@RequestBody @Valid RequestCreateAppointmentReport request) {
        UUID appointmentReportId = appointmentReportService.createAppointmentReport(request);
        return responseSupport.success(ResponseData.builder().data(appointmentReportId).build());
    }

    @PreAuthorize("hasPermission(null, 'CREATE_APPOINTMENT_REPORT')")
    @PostMapping("/submit-report")
    public ResponseEntity<ResponseData> submitAppointmentReport(@RequestBody @Valid RequestSubmitAppointmentReport request) {
        return responseSupport.success(ResponseData.builder().data(appointmentReportService.submitAppointmentReport(request)).build());
    }
}
