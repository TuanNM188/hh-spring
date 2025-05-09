package com.formos.huub.repository;

import com.formos.huub.domain.entity.ProjectUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectUpdateRepository extends JpaRepository<ProjectUpdate, UUID> {

    @Query("SELECT p FROM ProjectUpdate p where p.project.id = :projectId ORDER BY p.createdDate DESC")
    List<ProjectUpdate> findAllByProjectIdAndOrderByCreatedDateDesc(UUID projectId);

}
