package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.enums.StatusEnum;
import com.formos.huub.domain.request.funding.RequestSearchFunding;
import com.formos.huub.domain.request.portals.RequestCreatePortalFunding;
import com.formos.huub.domain.request.portals.RequestUpdatePortalFunding;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.framework.validation.constraints.UUIDCheck;
import com.formos.huub.service.fundingservice.FundingService;
import com.formos.huub.service.fundingservice.PortalFundingService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/funding")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FundingController {


    ResponseSupport responseSupport;

    FundingService fundingService;

    PortalFundingService portalFundingService;

    @GetMapping("/search")
    @PreAuthorize("hasPermission(null, 'SEARCH_FUNDING_BY_CONDITIONS')")
    public ResponseEntity<ResponseData> getAllFundingWithPageable(RequestSearchFunding request,
                                                                  @SortDefault(sort = "date_added", direction = Sort.Direction.DESC) Pageable pageable) {
        return responseSupport.success(ResponseData.builder().data(fundingService.searchFundingByConditions(request, pageable)).build());
    }

    @GetMapping("/related")
    public ResponseEntity<ResponseData> getRelatedFunding(RequestSearchFunding request) {
        return responseSupport.success(ResponseData.builder().data(fundingService.getRelatedFunding(request)).build());
    }

    @GetMapping("/{id}/detail")
    @PreAuthorize("hasPermission(null, 'GET_DETAIL_FUNDING_BY_ID')")
    public ResponseEntity<ResponseData> getDetailById(@PathVariable String id) {
        return responseSupport.success(ResponseData.builder().data(fundingService.getDetailFundingById(UUID.fromString(id))).build());
    }

    @PostMapping()
    @PreAuthorize("hasPermission(null, 'CREATE_PORTAL_FUNDING')")
    public ResponseEntity<ResponseData> createPortalFunding( @RequestBody @Valid RequestCreatePortalFunding request) {
        UUID id = portalFundingService.createPortalFunding( request);
        return responseSupport.success(ResponseData.builder().data(id).build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'GET_PORTAL_FUNDING')")
    public ResponseEntity<ResponseData> findPortalFundingById( @PathVariable @UUIDCheck String id) {
        ResponseData result = ResponseData.builder().data(portalFundingService.findPortalFundingById( UUID.fromString(id))).build();
        return responseSupport.success(result);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'UPDATE_PORTAL_FUNDING')")
    public ResponseEntity<ResponseData> updatePortalFunding(
        @PathVariable @UUIDCheck String id,
        @RequestBody @Valid RequestUpdatePortalFunding request) {
        portalFundingService.updatePortalFunding( UUID.fromString(id), request);
        return responseSupport.success();
    }
    
    @DeleteMapping("/{portalId}/{fundingId}")
    @PreAuthorize("hasPermission(null, 'DELETE_PORTAL_FUNDING')")
    public ResponseEntity<ResponseData> deletePortalFunding(@PathVariable @UUIDCheck String portalId, @PathVariable @UUIDCheck String fundingId) {
        portalFundingService.deletePortalFunding(UUID.fromString(portalId), UUID.fromString(fundingId));
        return responseSupport.success();
    }

    @PatchMapping("/{fundingId}/favorite")
    public ResponseEntity<ResponseData> favoriteFunding(@PathVariable String fundingId) {
        fundingService.favoriteFunding(UUID.fromString(fundingId), StatusEnum.ACTIVE);
        return responseSupport.success(ResponseData.builder().build());
    }

    @PatchMapping("/{fundingId}/un-favorite")
    public ResponseEntity<ResponseData> unFavoriteFunding(@PathVariable String fundingId) {
        fundingService.favoriteFunding(UUID.fromString(fundingId), StatusEnum.INACTIVE);
        return responseSupport.success(ResponseData.builder().build());
    }

    @PatchMapping("/{fundingId}/submitted")
    public ResponseEntity<ResponseData> submittedFunding(@PathVariable String fundingId) {
        fundingService.userSubmitFunding(UUID.fromString(fundingId), StatusEnum.ACTIVE);
        return responseSupport.success(ResponseData.builder().build());
    }

    @PatchMapping("/{fundingId}/un-submitted")
    public ResponseEntity<ResponseData> unSubmittedFunding(@PathVariable String fundingId) {
        fundingService.userSubmitFunding(UUID.fromString(fundingId), StatusEnum.INACTIVE);
        return responseSupport.success(ResponseData.builder().build());
    }
}
