package com.formos.huub.repository;

import com.formos.huub.domain.request.portals.RequestSearchPortals;
import com.formos.huub.domain.response.portals.SearchPortalsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PortalRepositoryCustom {

    Page<SearchPortalsResponse> searchByTermAndCondition(RequestSearchPortals condition, Pageable pageable);
}
