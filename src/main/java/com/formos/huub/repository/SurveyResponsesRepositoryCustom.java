package com.formos.huub.repository;

import com.formos.huub.domain.request.businessowner.RequestSearchBusinessOwnerSurveys;
import com.formos.huub.domain.request.survey.RequestSearchSurveyResponses;
import com.formos.huub.domain.response.survey.ResponseSearchSurveyResponses;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SurveyResponsesRepositoryCustom {
    Page<ResponseSearchSurveyResponses> searchSurveyResponsesByTermAndCondition(
        UUID surveyId,
        RequestSearchSurveyResponses request,
        Pageable pageable
    );

    Page<ResponseSearchSurveyResponses> searchSurveysResponsesByUserId(
        UUID userId,
        RequestSearchBusinessOwnerSurveys request,
        Pageable pageable
    );
}
