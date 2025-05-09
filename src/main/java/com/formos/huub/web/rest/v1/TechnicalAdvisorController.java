package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.request.invitemember.RequestInviteMember;
import com.formos.huub.domain.request.technicaladvisor.RequestBookingAppointment;
import com.formos.huub.domain.request.technicaladvisor.RequestListTechnicalAdvisor;
import com.formos.huub.domain.request.technicaladvisor.RequestSearchAdvisor;
import com.formos.huub.domain.request.technicaladvisor.RequestUpdateTechnicalAdvisor;
import com.formos.huub.framework.base.BaseController;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.framework.utils.HeaderUtils;
import com.formos.huub.framework.utils.UUIDUtils;
import com.formos.huub.framework.validation.constraints.UUIDCheck;
import com.formos.huub.service.invite.InviteService;
import com.formos.huub.service.technicaladvisor.TechnicalAdvisorService;
import jakarta.servlet.http.HttpServletRequest;
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
@RequestMapping("/technical-advisors")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TechnicalAdvisorController extends BaseController {

    ResponseSupport responseSupport;
    TechnicalAdvisorService technicalAdvisorService;
    InviteService inviteService;

    @PostMapping("/search")
    @PreAuthorize("hasPermission(null, 'SEARCH_TECHNICAL_ADVISOR_LIST')")
    public ResponseEntity<ResponseData> getAllTechnicalAdvisorsWithPageable(@Valid @RequestBody RequestListTechnicalAdvisor request, HttpServletRequest httpServletRequest) {
        var timezone = HeaderUtils.getTimezone(httpServletRequest);
        var response = technicalAdvisorService.getAllTechnicalAdvisorsWithPageable(request, timezone);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PostMapping("/browse-advisors")
    @PreAuthorize("hasPermission(null, 'SEARCH_BROWSE_TECHNICAL_ADVISOR_LIST')")
    public ResponseEntity<ResponseData> searchAllBrowseAdvisorsWithPageable(@RequestBody RequestSearchAdvisor request) {
        var response = technicalAdvisorService.searchBrowseAdvisorsWithPageable(request);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @GetMapping("/browse-advisors/count-filter")
    public ResponseEntity<ResponseData> countAdvisorUsingFilter(String technicalAssistanceId, String communityPartnerId) {
        var response = technicalAdvisorService.getCountAdvisorUsing(
            UUIDUtils.toUUID(technicalAssistanceId),
            UUIDUtils.toUUID(communityPartnerId)
        );
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @GetMapping("/browse-advisors/{technicalAdvisorId}")
    @PreAuthorize("hasPermission(null, 'GET_DETAIL_BROWSE_TECHNICAL_ADVISOR_BY_ID')")
    public ResponseEntity<ResponseData> getBrowseAdvisorById(@PathVariable String technicalAdvisorId) {
        var response = technicalAdvisorService.getDetailBrowseAdvisor(UUIDUtils.toUUID(technicalAdvisorId));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @GetMapping("/{technicalAdvisorId}/booking-setting")
    @PreAuthorize("hasPermission(null, 'GET_BOOKING_SETTING_OF_TECHNICAL_ADVISOR_BY_ID')")
    public ResponseEntity<ResponseData> getBookingSettingAdvisor(@PathVariable String technicalAdvisorId) {
        var response = technicalAdvisorService.getBookingAppointmentAdvisorForm(UUIDUtils.toUUID(technicalAdvisorId));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PostMapping("/booking-appointment")
    @PreAuthorize("hasPermission(null, 'BOOKING_APPOINTMENT_WITH_ADVISOR')")
    public ResponseEntity<ResponseData> bookingAppointmentAdvisor(
        @RequestBody RequestBookingAppointment request,
        HttpServletRequest httpServletRequest
    ) {
        var timezone = HeaderUtils.getTimezone(httpServletRequest);
        var response = technicalAdvisorService.bookingAppointmentAdvisor(request, timezone);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @GetMapping(path = "/{id}")
    @PreAuthorize(
        "hasPermission(null, 'GET_TECHNICAL_ADVISOR') or " +
            "(hasPermission(null, 'GET_TECHNICAL_ADVISOR_OWN') " +
            "and @ownerCheckSecurity.isTechnicalAdvisorUpdate(#id))"
    )
    public ResponseEntity<ResponseData> getTechnicalAdvisorById(@PathVariable(value = "id") @UUIDCheck final String id) {
        return responseSupport.success(
            ResponseData.builder().data(technicalAdvisorService.getTechnicalAdvisorById(UUID.fromString(id))).build()
        );
    }

    @PatchMapping("/{id}")
    @PreAuthorize(
        "hasPermission(null, 'UPDATE_TECHNICAL_ADVISOR') or " +
            "(hasPermission(null, 'UPDATE_TECHNICAL_ADVISOR_OWN') " +
            "and @ownerCheckSecurity.isTechnicalAdvisorUpdate(#id))"
    )
    public ResponseEntity<ResponseData> updateTechnicalAdvisor(
        @PathVariable(value = "id") final String id,
        @Valid @RequestBody RequestUpdateTechnicalAdvisor request
    ) {
        technicalAdvisorService.updateTechnicalAdvisor(UUID.fromString(id), request);
        return responseSupport.success();
    }

    @PostMapping("/invite")
    @PreAuthorize("hasPermission(null, 'INVITE_TECHNICAL_ADVISOR')")
    public ResponseEntity<ResponseData> inviteTechnicalAdvisor(@Valid @RequestBody RequestInviteMember request) {
        return responseSupport.success(ResponseData.builder().data(inviteService.inviteTechnicalAdvisor(request)).build());
    }

    @GetMapping("/assign/{communityPartnerId}")
    public ResponseEntity<ResponseData> getAssignedAdvisorsByCommunityPartnerId(@PathVariable String communityPartnerId) {
        return responseSupport.success(
            ResponseData.builder().data(technicalAdvisorService.getAssignedAdvisorsByCommunityPartnerId(communityPartnerId)).build()
        );
    }

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<ResponseData> getAdvisorsByAppointmentId(@PathVariable String appointmentId) {
        return responseSupport.success(
            ResponseData.builder().data(technicalAdvisorService.getAdvisorByAppointmentId(appointmentId)).build()
        );
    }

    @GetMapping(path = "/{id}/events")
    @PreAuthorize("hasPermission(null, 'GET_TECHNICAL_ADVISOR_EVENTS')")
    public ResponseEntity<ResponseData> getTechnicalAdvisorEventById(@PathVariable(value = "id") @UUIDCheck final String id) {
        return responseSupport.success(
            ResponseData.builder().data(technicalAdvisorService.getEvents(UUID.fromString(id))).build()
        );
    }
}
