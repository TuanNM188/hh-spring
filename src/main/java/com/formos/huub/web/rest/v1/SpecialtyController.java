package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.request.specialty.*;
import com.formos.huub.domain.response.specialty.ResponseSpecialtyArea;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.framework.validation.constraints.UUIDCheck;
import com.formos.huub.service.specialty.SpecialtyService;
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
@RequestMapping("/specialties")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SpecialtyController {

    ResponseSupport responseSupport;

    SpecialtyService specialtyService;

    @GetMapping()
    @PreAuthorize("hasPermission(null, 'GET_SPECIALTY_LIST')")
    public ResponseEntity<ResponseData> getAll(String isArea) {
        var response = specialtyService.getAll(isArea);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PostMapping("/technical-advisor/{technicalAdvisorId}")
    @PreAuthorize("hasPermission(null, 'CREATE_SPECIALTY_FOR_TECHNICAL_ADVISOR')")
    public ResponseEntity<ResponseData> saveSpecialtyForTA(@PathVariable String technicalAdvisorId, @RequestBody RequestUpdateSpecialtyForTA request) {
        specialtyService.saveSpecialtyForTechnicalAdvisor(UUID.fromString(technicalAdvisorId), request.getSpecialties());
        return responseSupport.success(ResponseData.builder().build());
    }

    @GetMapping("/technical-advisor/{technicalAdvisorId}")
    @PreAuthorize("hasPermission(null, 'GET_SPECIALTY_FOR_TECHNICAL_ADVISOR')")
    public ResponseEntity<ResponseData> getSpecialtyForTA(@PathVariable String technicalAdvisorId) {
        var response = specialtyService.getAllByUserId(UUID.fromString(technicalAdvisorId));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PostMapping()
    @PreAuthorize("hasPermission(null, 'CREATE_SPECIALTY')")
    public ResponseEntity<ResponseData> createSpecialty(@RequestBody @Valid RequestCreateSpecialty request) {
        var response = specialtyService.createSpecialty(request);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PatchMapping("/{specialtyId}")
    @PreAuthorize("hasPermission(null, 'UPDATE_SPECIALTY')")
    public ResponseEntity<ResponseData> updateSpecialty(@PathVariable @UUIDCheck String specialtyId,
                                                        @RequestBody @Valid RequestUpdateSpecialty request) {
        var response = specialtyService.updateSpecialty(UUID.fromString(specialtyId), request);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @DeleteMapping("/{specialtyId}")
    @PreAuthorize("hasPermission(null, 'DELETE_SPECIALTY')")
    public ResponseEntity<ResponseData> deleteSpecialty(@PathVariable @UUIDCheck String specialtyId) {
        specialtyService.deleteSpecialty(UUID.fromString(specialtyId));
        return responseSupport.success();
    }

    @GetMapping("/{specialtyId}/specialty-area")
    @PreAuthorize("hasPermission(null, 'GET_SPECIALTY_SPECIALTY_AREA')")
    public ResponseEntity<ResponseData> getAllBySpecialtyId(@PathVariable @UUIDCheck String specialtyId) {
        return responseSupport.success(ResponseData.builder().data(specialtyService.getAllBySpecialtyId(UUID.fromString(specialtyId))).build());
    }

    @PostMapping("/{specialtyId}/specialty-area")
    @PreAuthorize("hasPermission(null, 'CREATE_SPECIALTY_SPECIALTY_AREA')")
    public ResponseEntity<ResponseData> createSpecialtyArea(@PathVariable @UUIDCheck String specialtyId,
                                                            @RequestBody @Valid RequestCreateSpecialtyArea request) {
        ResponseSpecialtyArea response = specialtyService.createSpecialtyArea(UUID.fromString(specialtyId), request);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PatchMapping("/specialty-area/{specialtyAreaId}")
    @PreAuthorize("hasPermission(null, 'UPDATE_SPECIALTY_SPECIALTY_AREA')")
    public ResponseEntity<ResponseData> updateSpecialtyArea(@PathVariable @UUIDCheck String specialtyAreaId,
                                                            @RequestBody @Valid RequestUpdateSpecialtyArea request) {
        ResponseSpecialtyArea response = specialtyService.updateSpecialtyArea(UUID.fromString(specialtyAreaId), request);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @DeleteMapping("/specialty-area/{specialtyAreaId}")
    @PreAuthorize("hasPermission(null, 'DELETE_SPECIALTY_SPECIALTY_AREA')")
    public ResponseEntity<ResponseData> deleteSpecialtyArea(@PathVariable @UUIDCheck String specialtyAreaId) {
        specialtyService.deleteSpecialtyArea(UUID.fromString(specialtyAreaId));
        return responseSupport.success();
    }
}
