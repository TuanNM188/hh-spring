package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.request.serviceoutcome.RequestCreateServiceOutcome;
import com.formos.huub.domain.request.serviceoutcome.RequestUpdateServiceOutcome;
import com.formos.huub.domain.response.serviceoutcome.ResponseServiceOutcome;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.framework.validation.constraints.UUIDCheck;
import com.formos.huub.service.serviceoutcome.ServiceOutcomeService;
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
@RequestMapping("/service-outcomes")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ServiceOutcomeController {

    ServiceOutcomeService serviceOutcomeService;
    ResponseSupport responseSupport;

    @GetMapping("/{serviceOfferedId}/services")
    @PreAuthorize("hasPermission(null, 'GET_SERVICE_OUTCOME_SERVICES')")
    public ResponseEntity<ResponseData> getAllByServiceOfferedId(@PathVariable @UUIDCheck String serviceOfferedId) {
        return responseSupport.success(ResponseData.builder().data(serviceOutcomeService.getAllByServiceOfferedId(UUID.fromString(serviceOfferedId))).build());
    }

    @PostMapping("/{serviceOfferedId}/services")
    @PreAuthorize("hasPermission(null, 'CREATE_SERVICE_OUTCOME_SERVICES')")
    public ResponseEntity<ResponseData> createServiceOutcome(@PathVariable @UUIDCheck String serviceOfferedId,
                                                             @RequestBody @Valid RequestCreateServiceOutcome request) {
        ResponseServiceOutcome response = serviceOutcomeService.createServiceOutcome(UUID.fromString(serviceOfferedId), request);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PatchMapping("/{serviceId}")
    @PreAuthorize("hasPermission(null, 'UPDATE_SERVICE_OUTCOME')")
    public ResponseEntity<ResponseData> updateServiceOutcome(@PathVariable @UUIDCheck String serviceId,
                                                             @RequestBody @Valid RequestUpdateServiceOutcome request) {
        ResponseServiceOutcome response = serviceOutcomeService.updateServiceOutcome(UUID.fromString(serviceId), request);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @DeleteMapping("/{serviceId}")
    @PreAuthorize("hasPermission(null, 'DELETE_SERVICE_OUTCOME')")
    public ResponseEntity<ResponseData> deleteServiceOutcome(@PathVariable @UUIDCheck String serviceId) {
        serviceOutcomeService.deleteServiceOutcome(UUID.fromString(serviceId));
        return responseSupport.success();
    }
}
