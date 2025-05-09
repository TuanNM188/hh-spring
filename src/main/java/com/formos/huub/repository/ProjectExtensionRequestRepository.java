package com.formos.huub.repository;

import com.formos.huub.domain.entity.ProjectExtensionRequest;
import com.formos.huub.domain.enums.ProjectExtensionRequestStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectExtensionRequestRepository extends JpaRepository<ProjectExtensionRequest, UUID> {
    @Query("select e from ProjectExtensionRequest  e join e.project p where p.id = :id and e.status = :status")
    Optional<ProjectExtensionRequest> findByProjectIdAndStatus(UUID id, ProjectExtensionRequestStatus status);

    long countByProject_id(UUID projectId);
}
