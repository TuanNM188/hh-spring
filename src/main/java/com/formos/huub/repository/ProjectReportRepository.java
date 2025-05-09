package com.formos.huub.repository;

import com.formos.huub.domain.entity.ProjectReport;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectReportRepository extends JpaRepository<ProjectReport, UUID> {

    @Query("select pr from ProjectReport  pr join pr.project p where p.id = :projectId and p.isDelete = false")
    Optional<ProjectReport> findByProjectId(UUID projectId);
}
