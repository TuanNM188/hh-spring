package com.formos.huub.repository;

import com.formos.huub.domain.entity.PortalIntakeQuestion;
import com.formos.huub.domain.enums.FormCodeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PortalIntakeQuestionRepository extends JpaRepository<PortalIntakeQuestion, UUID> {

    void deleteAllByPortalId(UUID portalId);

    @Query(value = "SELECT piq FROM PortalIntakeQuestion piq JOIN Question q ON q.id = piq.questionId WHERE piq.portalId = :portalId AND q.formCode = :formCode")
    List<PortalIntakeQuestion> getAllByPortalIdAndFormCodes(UUID portalId, FormCodeEnum formCode);

    @Query(value = "SELECT piq FROM PortalIntakeQuestion piq WHERE piq.portalId = :portalId AND piq.questionId IN (:questionIds)")
    List<PortalIntakeQuestion> getAllByPortalIdAndQuestionIds(UUID portalId, List<UUID> questionIds);
}
