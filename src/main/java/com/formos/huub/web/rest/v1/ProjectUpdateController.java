package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.request.project.RequestCreateProjectUpdate;
import com.formos.huub.domain.response.project.ResponseProjectUpdate;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.service.project.ProjectUpdateService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/project-updates")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectUpdateController {

    private final ProjectUpdateService projectUpdateService;

    private final ResponseSupport responseSupport;

    @PostMapping
    @PreAuthorize("hasPermission(null, 'CREATE_PROJECT_UPDATE')")
    public ResponseEntity<ResponseData> create(@RequestBody RequestCreateProjectUpdate request) {
        projectUpdateService.create(request);
        return responseSupport.success();
    }

    @GetMapping("/{projectId}")
    @PreAuthorize("hasPermission(null, 'GET_PROJECT_UPDATE')")
    public ResponseEntity<ResponseData> findAllByOrderByCreatedDateDesc(@PathVariable String projectId) {
        List<ResponseProjectUpdate> result = projectUpdateService.findAllByOrderByCreatedDateDesc(UUID.fromString(projectId));
        return responseSupport.success(ResponseData.builder().data(result).build());
    }
}
