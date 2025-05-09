package com.formos.huub.repository;

import com.formos.huub.domain.entity.SurveyResponses;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyResponsesRepository extends JpaRepository<SurveyResponses, UUID>, SurveyResponsesRepositoryCustom {}
