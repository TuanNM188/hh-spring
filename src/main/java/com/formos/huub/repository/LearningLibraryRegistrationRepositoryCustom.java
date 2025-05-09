package com.formos.huub.repository;

import com.formos.huub.domain.request.Learninglibraryregistration.RequestSearchLearningLibraryRegistration;
import com.formos.huub.domain.request.Learninglibraryregistration.RequestSearchLessonSurvey;
import com.formos.huub.domain.request.businessowner.RequestSearchCourseRegistrations;
import com.formos.huub.domain.request.businessowner.RequestSearchCourseSurveys;
import com.formos.huub.domain.response.businessowner.ResponseSearchCourseRegistrations;
import com.formos.huub.domain.response.learninglibraryregistration.ResponseSearchLearningLibraryRegistration;
import com.formos.huub.domain.response.learninglibraryregistration.ResponseSearchLessonSurvey;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LearningLibraryRegistrationRepositoryCustom {
    Page<ResponseSearchLearningLibraryRegistration> searchRegistrationByTermAndCondition(
        RequestSearchLearningLibraryRegistration request,
        UUID portalId,
        Pageable pageable
    );

    Page<ResponseSearchLessonSurvey> searchLessonSurveyByTermAndCondition(
        RequestSearchLessonSurvey request,
        UUID portalId,
        Pageable pageable
    );

    Page<ResponseSearchCourseRegistrations> searchCourseRegistrations(
        UUID userUId,
        RequestSearchCourseRegistrations request,
        Pageable pageable
    );

    Page<ResponseSearchLessonSurvey> searchCourseSurveys(UUID userUId, RequestSearchCourseSurveys request, Pageable pageable);
}
