package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.request.serviceoffered.RequestCreateServiceOffered;
import com.formos.huub.domain.request.serviceoffered.RequestUpdateServiceOffered;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.framework.validation.constraints.UUIDCheck;
import com.formos.huub.service.serviceoffered.ServiceOfferedService;
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
@RequestMapping("/service-offereds")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ServiceOfferedController {

    ServiceOfferedService serviceOfferedService;

    ResponseSupport responseSupport;


    @GetMapping("/{categoryId}/services")
    public ResponseEntity<ResponseData> getAllServices(@PathVariable @UUIDCheck String categoryId) {
        return responseSupport.success(ResponseData.builder().data(serviceOfferedService.getAllByCategoryId(UUID.fromString(categoryId))).build());
    }

    @PostMapping("/{categoryId}/services")
    @PreAuthorize("hasPermission(null, 'CREATE_SERVICE_OFFERED_SERVICES')")
    public ResponseEntity<ResponseData> createService(@PathVariable @UUIDCheck String categoryId,
                                                      @RequestBody @Valid RequestCreateServiceOffered request) {
        var response = serviceOfferedService.createService(UUID.fromString(categoryId), request);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PatchMapping("/{serviceId}")
    @PreAuthorize("hasPermission(null, 'UPDATE_SERVICE_OFFERED')")
    public ResponseEntity<ResponseData> updateCategories(@PathVariable @UUIDCheck String serviceId,
                                                         @RequestBody @Valid RequestUpdateServiceOffered request) {
        var response = serviceOfferedService.updateService(UUID.fromString(serviceId), request);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @DeleteMapping("/{serviceId}")
    @PreAuthorize("hasPermission(null, 'DELETE_SERVICE_OFFERED')")
    public ResponseEntity<ResponseData> deleteCategory(@PathVariable @UUIDCheck String serviceId) {
        serviceOfferedService.deleteServiceOffered(UUID.fromString(serviceId));
        return responseSupport.success();
    }
}
