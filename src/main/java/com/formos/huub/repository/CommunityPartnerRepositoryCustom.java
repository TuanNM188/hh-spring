package com.formos.huub.repository;

import com.formos.huub.domain.request.RequestSearchCommunityPartner;
import com.formos.huub.domain.response.communitypartner.ResponseSearchCommunityPartner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommunityPartnerRepositoryCustom {

    Page<ResponseSearchCommunityPartner> searchByTermAndCondition(RequestSearchCommunityPartner condition, Pageable pageable);
}
