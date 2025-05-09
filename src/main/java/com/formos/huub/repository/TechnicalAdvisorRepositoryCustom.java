package com.formos.huub.repository;

import com.formos.huub.domain.request.technicaladvisor.RequestListTechnicalAdvisor;
import com.formos.huub.domain.response.technicaladvisor.ResponseTechnicalAdvisor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TechnicalAdvisorRepositoryCustom {

    Page<ResponseTechnicalAdvisor> getAllTechnicalAdvisorsWithPageable(
        RequestListTechnicalAdvisor request,
        UUID portalId,
        Pageable pageable
    );


}
