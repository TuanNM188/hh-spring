package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.request.projectreport.RequestCreateProjectReport;
import com.formos.huub.domain.request.projectreport.RequestUpdateProjectReport;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.framework.validation.constraints.UUIDCheck;
import com.formos.huub.service.projectreport.ProjectReportService;
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
@RequestMapping("/project-report")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectReportController {

    ResponseSupport responseSupport;

    ProjectReportService projectReportService;

    @PostMapping
    @PreAuthorize("hasPermission(null, 'CREATE_PROJECT_REPORT')")
    public ResponseEntity<ResponseData> create(@RequestBody @Valid RequestCreateProjectReport request) {
        UUID projectReportId = projectReportService.createProjectReport(request);
        return responseSupport.success(ResponseData.builder().data(projectReportId).build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'UPDATE_PROJECT_REPORT')")
    public ResponseEntity<ResponseData> update(@PathVariable @UUIDCheck String id, @RequestBody @Valid RequestUpdateProjectReport request) {
        var response = projectReportService.updateProjectReport(UUID.fromString(id), request);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'GET_PROJECT_REPORT')")
    public ResponseEntity<ResponseData> getDetail(@PathVariable String id) {
        var response = projectReportService.getProjectReport(UUID.fromString(id));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }
}
