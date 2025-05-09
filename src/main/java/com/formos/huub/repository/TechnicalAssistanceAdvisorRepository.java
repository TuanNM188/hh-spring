package com.formos.huub.repository;

import com.formos.huub.domain.entity.TechnicalAdvisor;
import com.formos.huub.domain.entity.TechnicalAssistanceAdvisor;
import com.formos.huub.domain.entity.TechnicalAssistanceSubmit;
import com.formos.huub.domain.entity.User;
import com.formos.huub.domain.entity.embedkey.TechnicalAssistanceAdvisorEmbedKey;
import com.formos.huub.domain.response.technicaladvisor.IResponseTechnicalAdvisorInfo;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TechnicalAssistanceAdvisorRepository
    extends JpaRepository<TechnicalAssistanceAdvisor, TechnicalAssistanceAdvisorEmbedKey> {
    @Query(
        "select ta.id as technicalAdvisorId, u.normalizedFullName as fullName, u.id as userId," +
        "  u.imageUrl as imageUrl from TechnicalAssistanceAdvisor " +
        " taa join taa.id.technicalAdvisor ta join ta.user u " +
        " join taa.id.technicalAssistanceSubmit tas where tas.id = :technicalAssistanceId"
    )
    List<IResponseTechnicalAdvisorInfo> getAllAdvisorByTechnicalAssistance(UUID technicalAssistanceId);

    @Modifying
    void deleteById_TechnicalAssistanceSubmit_Id(UUID technicalAssistanceId);

    @Query("SELECT ta.id.technicalAdvisor FROM TechnicalAssistanceAdvisor ta WHERE ta.id.technicalAssistanceSubmit = :submit")
    List<TechnicalAdvisor> findTechnicalAdvisorsByTechnicalAssistanceSubmit(@Param("submit") TechnicalAssistanceSubmit submit);
}
