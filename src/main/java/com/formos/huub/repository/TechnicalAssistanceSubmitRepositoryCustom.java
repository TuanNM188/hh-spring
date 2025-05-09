package com.formos.huub.repository;

import com.formos.huub.domain.request.technicalassistance.RequestSearchTechnicalApplicationSubmit;
import com.formos.huub.domain.response.technicalassistance.ResponseSearchApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TechnicalAssistanceSubmitRepositoryCustom {


    Page<ResponseSearchApplication> getAllApplicationProjectWithPageable(
        RequestSearchTechnicalApplicationSubmit request,
        Pageable pageable
    );
}
