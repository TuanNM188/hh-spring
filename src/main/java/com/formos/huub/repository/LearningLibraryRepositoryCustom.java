package com.formos.huub.repository;

import com.formos.huub.domain.request.learninglibrary.RequestSearchLearningLibrary;
import com.formos.huub.domain.response.learninglibrary.ResponseSearchLearningLibrary;
import com.formos.huub.domain.response.learninglibrary.ResponseSearchLearningLibraryCardView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface LearningLibraryRepositoryCustom {

    Page<ResponseSearchLearningLibrary> searchByTermAndCondition(RequestSearchLearningLibrary condition, UUID portalId, Pageable pageable);

    Page<ResponseSearchLearningLibraryCardView> searchByTermAndConditionCardView(RequestSearchLearningLibrary condition, UUID userId, UUID portalId, Pageable pageable);

}
