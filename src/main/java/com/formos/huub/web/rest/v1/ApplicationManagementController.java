package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.request.technicalassistance.RequestApprovalApplication;
import com.formos.huub.domain.request.technicalassistance.RequestSearchTechnicalApplicationSubmit;
import com.formos.huub.domain.request.technicalassistance.RequestUpdateTechnicalAssistance;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.framework.utils.HeaderUtils;
import com.formos.huub.framework.utils.UUIDUtils;
import com.formos.huub.service.applicationmanagement.ApplicationManagementService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/application-managements")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApplicationManagementController {

    ResponseSupport responseSupport;

    ApplicationManagementService applicationManagementService;

    @PostMapping("/search")
    @PreAuthorize("hasPermission(null, 'SEARCH_ALL_APPLICATIONS')")
    public ResponseEntity<ResponseData> searchAllApprovedApplications(@RequestBody RequestSearchTechnicalApplicationSubmit request, HttpServletRequest httpServletRequest) {
        request.setTimezone(HeaderUtils.getTimezone(httpServletRequest));
        var response = applicationManagementService.searchAllApplicationWithProjects(request);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @GetMapping("/overview")
    @PreAuthorize("hasPermission(null, 'GET_OVERVIEW_TECHNICAL_ASSISTANCE')")
    public ResponseEntity<ResponseData> getTechnicalAssistanceOverview(String portalId) {
        return responseSupport.success(
            ResponseData.builder().data(applicationManagementService.getTechnicalAssistanceOverview(UUIDUtils.toUUID(portalId))).build()
        );
    }

    @GetMapping("/applications-approved/{portalId}")
    public ResponseEntity<ResponseData> getAllTechnicalAssistanceApproved(@PathVariable String portalId) {
        return responseSupport.success(
            ResponseData.builder().data(applicationManagementService.getAllTechnicalAssistanceApproved(UUIDUtils.toUUID(portalId))).build()
        );
    }

    @GetMapping("/{programTermId}/applications/{userId}")
    @PreAuthorize("hasPermission(null, 'GET_ALL_APPLICATIONS_META')")
    public ResponseEntity<ResponseData> getAllTechnicalAssistance(@PathVariable String programTermId, @PathVariable String userId) {
        var response = applicationManagementService.getAllTechnicalAssistance(UUIDUtils.toUUID(programTermId), UUIDUtils.toUUID(userId));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @GetMapping("/applications/{applicationId}")
    @PreAuthorize("hasPermission(null, 'GET_DETAIL_APPLICATION_BY_ID')")
    public ResponseEntity<ResponseData> getApplicationDetail(@PathVariable String applicationId) {
        var response = applicationManagementService.getApplicationDetail(UUID.fromString(applicationId));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PatchMapping("/applications/{applicationId}/revert-to-pending")
    @PreAuthorize("hasPermission(null, 'APPROVAL_APPLICATION')")
    public ResponseEntity<ResponseData> revertToPending(@PathVariable String applicationId) {
        applicationManagementService.revertToPending(UUID.fromString(applicationId));
        return responseSupport.success(ResponseData.builder().build());
    }

    @PatchMapping("/applications/approval")
    @PreAuthorize("hasPermission(null, 'APPROVAL_APPLICATION')")
    public ResponseEntity<ResponseData> approvalApplication(@RequestBody @Valid RequestApprovalApplication request) {
        applicationManagementService.processApprovalApplication(request);
        return responseSupport.success(ResponseData.builder().build());
    }

    @GetMapping("/applications/{programTermId}/vendors")
    public ResponseEntity<ResponseData> getProgramTermVendor(@PathVariable String programTermId) {
        var response = applicationManagementService.getTermVendorByTermId(UUIDUtils.toUUID(programTermId));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @GetMapping("/applications/{applicationId}/info")
    @PreAuthorize("hasPermission(null, 'GET_TECHNICAL_ASSISTANCE_INFO_BY_ID')")
    public ResponseEntity<ResponseData> getTechnicalAssistanceInfo(@PathVariable String applicationId) {
        var response = applicationManagementService.getInfoTechnicalAssistanceByTerm(UUIDUtils.toUUID(applicationId));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @GetMapping("/applications/{applicationId}/is-used")
    public ResponseEntity<ResponseData> checkUseApplication(@PathVariable String applicationId, String technicalAdvisorId) {
        var response = applicationManagementService.checkUseApplication(UUIDUtils.toUUID(applicationId), UUIDUtils.toUUID(technicalAdvisorId));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PatchMapping("/applications/{technicalAssistanceId}")
    @PreAuthorize("hasPermission(null, 'UPDATE_APPLICATION_AND_ASSIGN_VENDOR')"
        + "or @ownerCheckSecurity.isCommunityPartnerOwnerNavigator(authentication)")
    public ResponseEntity<ResponseData> updateAndAssignVendorForApplication(@PathVariable String technicalAssistanceId,
                                                                            @RequestBody RequestUpdateTechnicalAssistance request) {
        applicationManagementService.updateTechnicalAssistance(UUIDUtils.toUUID(technicalAssistanceId), request);
        return responseSupport.success(ResponseData.builder().build());
    }

}
