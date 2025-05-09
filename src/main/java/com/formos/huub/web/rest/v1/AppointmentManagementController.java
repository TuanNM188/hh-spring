/**
 * ***************************************************
 * * Description :
 * * File        : AppointmentController
 * * Author      : Hung Tran
 * * Date        : Jan 20, 2025
 * ***************************************************
 **/
package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.request.appointmentmanagement.RequestCreateFeedbackAppointment;
import com.formos.huub.domain.request.appointmentmanagement.RequestRescheduleAppointment;
import com.formos.huub.domain.request.appointmentmanagement.RequestSearchAppointment;
import com.formos.huub.domain.request.appointmentmanagement.RequestUpdateAppointmentDetail;
import com.formos.huub.framework.base.BaseController;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.framework.utils.HeaderUtils;
import com.formos.huub.framework.utils.UUIDUtils;
import com.formos.huub.service.appointmentmanagement.AppointmentManagementService;
import com.formos.huub.service.technicaladvisor.TechnicalAdvisorService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/appointment-managements")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppointmentManagementController extends BaseController {

    ResponseSupport responseSupport;

    AppointmentManagementService appointmentManagementService;
    TechnicalAdvisorService technicalAdvisorService;

    @PostMapping("/search")
    @PreAuthorize("hasPermission(null, 'SEARCH_APPOINTMENT_MANAGEMENTS')")
    public ResponseEntity<ResponseData> searchAppointments(@Valid @RequestBody RequestSearchAppointment request, HttpServletRequest httpServletRequest) {

        request.setTimezone(HeaderUtils.getTimezone(httpServletRequest));
        var response = appointmentManagementService.searchAppointments(request);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @GetMapping("/overview")
    @PreAuthorize("hasPermission(null, 'SEARCH_OVERVIEW_APPOINTMENT_MANAGEMENTS')")
    public ResponseEntity<ResponseData> getOverviewAppointmentInTerm(String portalId, String startDate,
                                                                     String endDate, HttpServletRequest httpServletRequest) {
        var response = appointmentManagementService.getOverviewApplicationsByTerm(UUIDUtils.toUUID(portalId), startDate, endDate, HeaderUtils.getTimezone(httpServletRequest));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PostMapping("/feedback")
    @PreAuthorize("hasPermission(null, 'CREATE_FEEDBACK_APPOINTMENT')")
    public ResponseEntity<ResponseData> createFeedback(@Valid @RequestBody RequestCreateFeedbackAppointment request) {
        appointmentManagementService.createFeedback(request);
        return responseSupport.success();
    }

    @GetMapping("/{id}/detail")
    @PreAuthorize("hasPermission(null, 'SEARCH_OVERVIEW_APPOINTMENT')")
    public ResponseEntity<ResponseData> getAppointmentDetail(@PathVariable String id) {
        var response = appointmentManagementService.getAppointmentDetail(UUIDUtils.toUUID(id));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PatchMapping("/{id}/detail")
    @PreAuthorize("hasPermission(null, 'SEARCH_OVERVIEW_APPOINTMENT')")
    public ResponseEntity<ResponseData> updateAppointmentDetail(
        @PathVariable String id,
        @Valid @RequestBody RequestUpdateAppointmentDetail request
    ) {
        appointmentManagementService.updateAppointmentDetail(UUIDUtils.toUUID(id), request);
        return responseSupport.success(ResponseData.builder().build());
    }

    @GetMapping("/{id}/advisor-booking-setting")
    @PreAuthorize("hasPermission(null, 'SEARCH_OVERVIEW_APPOINTMENT')")
    public ResponseEntity<ResponseData> getAdvisorBookingSettingToReschedule(@PathVariable String id) {
        var response = appointmentManagementService.getAdvisorBookingSettingToReschedule(UUIDUtils.toUUID(id));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PatchMapping("/{id}/reschedule")
    @PreAuthorize("hasPermission(null, 'SEARCH_OVERVIEW_APPOINTMENT')")
    public ResponseEntity<ResponseData> rescheduleAppointment(
        @PathVariable String id,
        @Valid @RequestBody RequestRescheduleAppointment request, HttpServletRequest httpServletRequest
    ) {
        var timezone = HeaderUtils.getTimezone(httpServletRequest);
        var result = technicalAdvisorService.rescheduleAppointment(UUIDUtils.toUUID(id), request, timezone);
        return responseSupport.success(ResponseData.builder().data(result).build());
    }

    @GetMapping("/{id}/detail/header")
    @PreAuthorize("hasPermission(null, 'SEARCH_OVERVIEW_APPOINTMENT')")
    public ResponseEntity<ResponseData> getAppointmentDetailHeader(@PathVariable String id) {
        var response = appointmentManagementService.getHeaderAppointmentDetail(UUIDUtils.toUUID(id));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @GetMapping("/check-exist/{appointmentId}")
    public ResponseEntity<ResponseData> checkExistingAppointment(@PathVariable String appointmentId) {
        var response = appointmentManagementService.existsByAppointmentId(UUID.fromString(appointmentId));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @GetMapping("/check-exist/{appointmentId}/user")
    public ResponseEntity<ResponseData> checkExistingAppointmentWithUserId(@PathVariable String appointmentId) {
        var response = appointmentManagementService.checkExistingAppointmentWithUserId(UUID.fromString(appointmentId));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @GetMapping("/check-access/{appointmentId}/advisor")
    @PreAuthorize("hasPermission(null, 'CHECK_ACCESS_APPOINTMENT_TECHNICAL_ADVISOR')")
    public ResponseEntity<ResponseData> checkAccessAppointmentForTechnicalAdvisor(@PathVariable String appointmentId) {
        var isAccess = appointmentManagementService.hasAdvisorAccessToAppointment(UUID.fromString(appointmentId));
        return isAccess ? responseSupport.success() : responseSupport.failed(HttpStatus.FORBIDDEN, MessageHelper.getMessage(Message.Keys.E0006));
    }

}
