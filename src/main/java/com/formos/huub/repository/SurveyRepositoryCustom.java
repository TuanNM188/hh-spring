package com.formos.huub.repository;

import com.formos.huub.domain.request.survey.RequestSearchSurvey;
import com.formos.huub.domain.response.survey.ResponseSearchSurvey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SurveyRepositoryCustom {
    Page<ResponseSearchSurvey> searchSurveyByTermAndCondition(RequestSearchSurvey request, Pageable pageable);
}
