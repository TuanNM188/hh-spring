package com.formos.huub.repository;

import com.formos.huub.domain.entity.Program;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProgramRepository extends JpaRepository<Program, UUID> {

    @Modifying
    @Query("update Program v SET v.isDelete =  true  where v.portal.id = :portalId")
    void deleteByPortalId(UUID portalId);

    Optional<Program> findByPortalId(UUID portalId);
}
