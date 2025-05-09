/**
 * ***************************************************
 * * Description :
 * * File        : ProjectController
 * * Author      : Hung Tran
 * * Date        : Jan 20, 2025
 * ***************************************************
 **/
package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.request.project.RequestCreateFeedbackProject;
import com.formos.huub.domain.request.project.RequestCreateProject;
import com.formos.huub.domain.request.project.RequestUpdateProject;
import com.formos.huub.domain.request.project.RequestSearchProject;
import com.formos.huub.framework.base.BaseController;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.framework.utils.HeaderUtils;
import com.formos.huub.framework.validation.constraints.UUIDCheck;
import com.formos.huub.framework.utils.UUIDUtils;
import com.formos.huub.service.project.ProjectService;
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
@RequestMapping("/projects")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectController extends BaseController {

    ResponseSupport responseSupport;

    ProjectService projectService;

    @PostMapping("/search")
    @PreAuthorize("hasPermission(null, 'SEARCH_PROJECT_MANAGEMENTS')")
    public ResponseEntity<ResponseData> searchAppointments(@Valid @RequestBody RequestSearchProject request, HttpServletRequest httpServletRequest) {
        request.setTimezone(HeaderUtils.getTimezone(httpServletRequest));
        var response = projectService.searchProjects(request);

        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @GetMapping("/overview")
    @PreAuthorize("hasPermission(null, 'OVERVIEW_PROJECT_MANAGEMENTS')")
    public ResponseEntity<ResponseData> getOverviewAppointmentInTerm(String portalId) {
        var response = projectService.countOverviewProject(UUIDUtils.toUUID(portalId));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PostMapping("/feedback")
    @PreAuthorize("hasPermission(null, 'CREATE_FEEDBACK_PROJECT')")
    public ResponseEntity<ResponseData> createFeedback(@Valid @RequestBody RequestCreateFeedbackProject request) {
        projectService.createFeedback(request);
        return responseSupport.success();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'GET_PROJECT_DETAIL')")
    public ResponseEntity<ResponseData> getDetail(@PathVariable String id) {
        var response = projectService.getDetail(UUID.fromString(id));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PostMapping("")
    @PreAuthorize("hasPermission(null, 'CREATE_PROJECT')")
    public ResponseEntity<ResponseData> create(@Valid @RequestBody RequestCreateProject request) {
        projectService.create(request);
        return responseSupport.success();
    }

    @PutMapping
    @PreAuthorize("hasPermission(null, 'UPDATE_PROJECT')")
    public ResponseEntity<ResponseData> update(@Valid @RequestBody RequestUpdateProject request) {
        var response = projectService.update(request);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @GetMapping("/header/{id}")
    public ResponseEntity<ResponseData> getProjectDetailHeader(@PathVariable String id) {
        var response = projectService.getProjectHeader(UUID.fromString(id));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasPermission(null, 'APPROVE_PROJECT')")
    public ResponseEntity<ResponseData> approveProject(@PathVariable String id) {
        projectService.approveProject(UUID.fromString(id));
        return responseSupport.success();
    }

    @PatchMapping("/{id}/deny")
    @PreAuthorize("hasPermission(null, 'DENY_PROJECT')")
    public ResponseEntity<ResponseData> denyProject(@PathVariable String id) {
        projectService.denyProject(UUID.fromString(id));
        return responseSupport.success();
    }

    @PostMapping("/additional-scope-work")
    @PreAuthorize("hasPermission(null, 'ADDITIONAL_SCOPE_WORK')")
    public ResponseEntity<ResponseData> AdditionalScopeWork(@Valid @RequestBody RequestCreateProject request) {
        projectService.create(request);
        return responseSupport.success();
    }

    @GetMapping("/check-exist/{projectId}")
    public ResponseEntity<ResponseData> checkExistingProject(@PathVariable String projectId) {
        var response = projectService.existsByProjectId(UUID.fromString(projectId));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @GetMapping("/check-exist/{projectId}/business-owner")
    public ResponseEntity<ResponseData> checkExistingProjectWithBusinessOwnerId(@PathVariable String projectId) {
        var isExits = projectService.checkExistingProjectWithBusinessOwnerId(UUID.fromString(projectId));
        return isExits ? responseSupport.success() : responseSupport.failed(HttpStatus.FORBIDDEN, MessageHelper.getMessage(Message.Keys.E0006));
    }
}
