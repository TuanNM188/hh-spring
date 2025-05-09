package com.formos.huub.repository;

import com.formos.huub.domain.entity.ProgramTerm;
import com.formos.huub.domain.entity.User;
import com.formos.huub.domain.enums.StatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProgramTermRepository extends JpaRepository<ProgramTerm, UUID> {

    @Modifying
    @Query("update ProgramTerm v SET v.isDelete =  true  where v.program.portal.id = :portalId")
    void deleteByPortalId(UUID portalId);

    @Query("select p.programManager from ProgramTerm p where p.program.portal.id = :portalId")
    List<User> findAllProgramManagerByPortalId(UUID portalId);

    @Query("select sum(pt.budget) from ProgramTerm pt join pt.program pr join pr.portal p " +
        " where (:portalId is null or p.id =:portalId) and pt.status = :status")
    BigDecimal sumAllTermByPortalAndStatus(UUID portalId, StatusEnum status);

    @Query(value = "select pt.* from program_term pt " +
        " join program p on p.id = pt.program_id and pt.is_delete is false and p.is_delete is false" +
        " where pt.status ='ACTIVE' and pt.is_current is true" +
        " and p.id = :programId limit 1", nativeQuery = true)
    Optional<ProgramTerm> findCurrentByProgramId(UUID programId);

    @Query(value = "select pt.* from program_term pt " +
        " join program p on p.id = pt.program_id and pt.is_delete is false and p.is_delete is false" +
        " where pt.status ='ACTIVE' and pt.start_date > current_date and pt.is_current is false" +
        " and p.id = :programId order by pt.start_date asc limit 1", nativeQuery = true)
    Optional<ProgramTerm> findNextByProgramId(UUID programId);

    @Query("select pt from ProgramTerm pt join pt.program pr join pr.portal p where p.id = :portalId" +
        " and pt.status = 'ACTIVE'")
    List<ProgramTerm> findAllTermActiveByPortalId(UUID portalId);

    @Query("select pt from ProgramTerm pt join pt.program pr join pr.portal p where p.id = :portalId order by pt.startDate desc")
    List<ProgramTerm> findAllTermByPortalId(UUID portalId);

    @Query("select pt from ProgramTerm pt join pt.program p join p.portal portal where portal.id = :id and pt.status = :programTermStatus")
    Optional<ProgramTerm> getByPortalId(UUID id, StatusEnum programTermStatus);
}
