package com.formos.huub.repository;

import com.formos.huub.domain.request.directmessage.RequestSearchReferralMessage;
import com.formos.huub.domain.response.directmessage.ResponseSearchReferralMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReferralMessageRepositoryCustom {

    Page<ResponseSearchReferralMessage> searchByTermAndCondition(RequestSearchReferralMessage condition, Pageable pageable);
}
