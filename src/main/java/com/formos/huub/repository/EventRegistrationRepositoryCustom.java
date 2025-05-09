package com.formos.huub.repository;

import com.formos.huub.domain.request.businessowner.RequestSearchEventRegistrations;
import com.formos.huub.domain.response.eventregistration.ResponseSearchEventRegistration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventRegistrationRepositoryCustom {

    Page<ResponseSearchEventRegistration> searchByTermAndCondition(RequestSearchEventRegistrations condition,
                                                                   Pageable pageable);
}
