package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.request.vendor.RequestSearchVendor;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.framework.utils.UUIDUtils;
import com.formos.huub.service.vendor.VendorService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/vendors")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VendorController {

    ResponseSupport responseSupport;

    VendorService vendorService;

    @PostMapping("/search")
    @PreAuthorize("hasPermission(null, 'SEARCH_ALL_VENDORS')")
    public ResponseEntity<ResponseData> searchAllApprovedApplications(@RequestBody RequestSearchVendor request) {
        var response = vendorService.searchAllVendorWithProjects(request);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @GetMapping("/overview")
    @PreAuthorize("hasPermission(null, 'GET_OVERVIEW_BUDGET_VENDOR')")
    public ResponseEntity<ResponseData> getTechnicalAssistanceOverview(String portalId, boolean isPercent) {
        return responseSupport.success(
            ResponseData.builder().data(vendorService.getOverviewBudgetVendor(UUIDUtils.toUUID(portalId), isPercent)).build()
        );
    }
}
