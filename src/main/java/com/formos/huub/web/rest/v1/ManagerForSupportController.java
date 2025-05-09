package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.request.project.RequestSearchProjectForBO;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.framework.utils.UUIDUtils;
import com.formos.huub.service.managerforsupport.ManagerForSupportService;
import com.formos.huub.service.portals.PortalService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/manage-1-1-support")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ManagerForSupportController {

    ResponseSupport responseSupport;
    ManagerForSupportService managerForSupportService;

    @GetMapping("/overview")
    @PreAuthorize("hasPermission(null, 'MANAGE_1_1_SUPPORT_OVERVIEW')")
    public ResponseEntity<ResponseData> getOverviewBusinessOwnerOManager() {
        var response = managerForSupportService.getOverviewBoManager();
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @GetMapping("/first-upcoming-appointment")
    @PreAuthorize("hasPermission(null, 'MANAGE_1_1_SUPPORT_APPOINTMENT')")
    public ResponseEntity<ResponseData> getFirstAppointmentUpcoming() {
        var response = managerForSupportService.getFirstAppointmentUpcoming();
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @GetMapping("/upcoming-appointment")
    @PreAuthorize("hasPermission(null, 'MANAGE_1_1_SUPPORT_APPOINTMENT')")
    public ResponseEntity<ResponseData> getAppointmentUpcoming(@RequestParam String ignoreAppointmentId, Pageable pageable) {
        var response = managerForSupportService.getAppointmentUpcoming(UUIDUtils.toUUID(ignoreAppointmentId), pageable);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PostMapping("/projects")
    @PreAuthorize("hasPermission(null, 'MANAGE_1_1_SUPPORT_PROJECTS')")
    public ResponseEntity<ResponseData> getProjectForBusinessOwner(@Valid @RequestBody RequestSearchProjectForBO request) {
        var response = managerForSupportService.getProjectForBusinessOwner(request);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

}

