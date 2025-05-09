package com.formos.huub.repository;

import com.formos.huub.domain.request.project.RequestSearchProject;
import com.formos.huub.domain.request.project.RequestSearchProjectForBO;
import com.formos.huub.domain.response.project.ResponseProjectForBusinessOwner;
import com.formos.huub.domain.request.tasurvey.RequestSearchTaSurveyProject;
import com.formos.huub.domain.response.project.ResponseSearchProject;
import com.formos.huub.domain.response.tasurvey.ResponseTaSurveyProject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectRepositoryCustom {

    Page<ResponseSearchProject> searchByTermAndCondition(RequestSearchProject condition, Pageable pageable);

    Page<ResponseProjectForBusinessOwner> searchProjectForBusinessOwner(RequestSearchProjectForBO condition, Pageable pageable);

    Page<ResponseTaSurveyProject> getRatingResponse(RequestSearchTaSurveyProject condition, Pageable pageable);
}
