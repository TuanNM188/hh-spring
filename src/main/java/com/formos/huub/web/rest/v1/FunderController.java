package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.request.funder.RequestCreateFunder;
import com.formos.huub.domain.request.funder.RequestUpdateFunder;
import com.formos.huub.domain.response.funder.ResponseFunder;
import com.formos.huub.domain.response.funder.ResponseFunderDetail;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.framework.validation.constraints.UUIDCheck;
import com.formos.huub.service.funder.FunderService;
import jakarta.validation.Valid;
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
@RequestMapping("/funders")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FunderController {

    FunderService funderService;

    ResponseSupport responseSupport;

    @GetMapping
    @PreAuthorize("hasPermission(null, 'GET_FUNDER_LIST')")
    public ResponseEntity<ResponseData> getAllFunder() {
        List<ResponseFunder> funders = funderService.getAll();
        return responseSupport.success(ResponseData.builder().data(funders).build());
    }

    @PostMapping
    @PreAuthorize("hasPermission(null, 'CREATE_FUNDER')")
    public ResponseEntity<ResponseData> createFunder(@RequestBody @Valid RequestCreateFunder request) {
        ResponseFunderDetail response = funderService.createFunder(request);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'UPDATE_FUNDER_DETAIL')")
    public ResponseEntity<ResponseData> updateFunder(@PathVariable @UUIDCheck String id,
                                                     @RequestBody @Valid RequestUpdateFunder request) {
        ResponseFunderDetail response = funderService.updateFunder(UUID.fromString(id), request);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'GET_FUNDER_DETAIL')")
    public ResponseEntity<ResponseData> findFunderById(@PathVariable @UUIDCheck String id) {
        ResponseFunderDetail response = funderService.findFunderById(UUID.fromString(id));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

}
