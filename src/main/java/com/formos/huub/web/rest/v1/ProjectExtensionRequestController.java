package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.request.project.RequestCreateProjectExtension;
import com.formos.huub.domain.request.project.RequestExtensionRequestProject;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.service.project.ProjectExtensionRequestService;
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
@RequestMapping("/extension-requests")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectExtensionRequestController {

    private final ProjectExtensionRequestService projectExtensionRequestService;

    private final ResponseSupport responseSupport;

    @PostMapping
    @PreAuthorize("hasPermission(null, 'CREATE_PROJECT_EXTENSION_REQUEST')")
    public ResponseEntity<ResponseData> createExtensionRequest(@Valid @RequestBody RequestCreateProjectExtension request) {

        var response = projectExtensionRequestService.createExtensionRequest(request);

        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'GET_PROJECT_EXTENSION_REQUEST')")
    public ResponseEntity<ResponseData> findByProjectId(@PathVariable String id) {
        var response = projectExtensionRequestService.findByProjectId(UUID.fromString(id));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @GetMapping("/program-term/{projectId}")
    public ResponseEntity<ResponseData> getProgramTerm(@PathVariable String projectId) {
        var response = projectExtensionRequestService.getProgramTermDateRange(UUID.fromString(projectId));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PostMapping("/approve")
    @PreAuthorize("hasPermission(null, 'APPROVE_PROJECT_EXTENSION_REQUEST')")
    public ResponseEntity<ResponseData> approveExtensionRequest(@RequestBody RequestExtensionRequestProject request) {

        projectExtensionRequestService.approveExtensionRequest(request);

        return responseSupport.success();
    }

    @PostMapping("/deny")
    @PreAuthorize("hasPermission(null, 'DENY_PROJECT_EXTENSION_REQUEST')")
    public ResponseEntity<ResponseData> denyExtensionRequest(@RequestBody RequestExtensionRequestProject request) {

        projectExtensionRequestService.denyExtensionRequest(request);

        return responseSupport.success();
    }
}
