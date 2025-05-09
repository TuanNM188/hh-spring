package com.formos.huub.web.rest.v1.APIPublic;

import com.formos.huub.domain.request.funding.RequestSearchFunding;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.service.fundingservice.FundingService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/public/fundings")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FundingPublicController {

    ResponseSupport responseSupport;
    FundingService fundingService;

    @GetMapping("/search")
    public ResponseEntity<ResponseData> getAllFundingWithPageable(
        RequestSearchFunding request,
        @SortDefault(sort = "date_added", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return responseSupport.success(ResponseData.builder().data(fundingService.searchFundingByConditions(request, pageable)).build());
    }
}
