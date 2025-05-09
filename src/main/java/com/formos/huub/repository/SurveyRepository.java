package com.formos.huub.repository;

import com.formos.huub.domain.entity.Survey;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyRepository extends JpaRepository<Survey, UUID>, SurveyRepositoryCustom {
    Boolean existsByNameIgnoreCase(String name);

    Boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

    @Query("SELECT count(s) > 0 " + "FROM Survey s " + "JOIN s.portal p " + "WHERE p.id = :portalId and s.id = :surveyId")
    boolean existsByPortalId(UUID portalId, UUID surveyId);
}
