package com.formos.huub.repository;

import com.formos.huub.domain.request.portals.RequestSearchPortalFunding;
import com.formos.huub.domain.response.portals.SearchPortalFundingResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PortalFundingRepositoryCustom {

    Page<SearchPortalFundingResponse> searchByTermAndCondition(RequestSearchPortalFunding condition, Pageable pageable);
}
