package com.formos.huub.repository;

import com.formos.huub.domain.entity.LearningLibraryStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LearningLibraryStepRepository extends JpaRepository<LearningLibraryStep, UUID> {

}
