package com.formos.huub.repository;

import com.formos.huub.domain.entity.LearningLibrarySection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LearningLibrarySectionRepository extends JpaRepository<LearningLibrarySection, UUID> {

    @Query(value = "select COUNT(1) AS numDownloads from " +
        " learning_library_section lls inner join " +
        " learning_library_lesson lll on lll.id  = lls.learning_library_lesson_id and lll.is_delete is false and lls.section_type  = 'FILES' " +
        " inner join learning_library_step s on lll.learning_library_step_id  = s.id   and s.is_delete is false, " +
        " json_array_elements(lls.contents ->'sectionFiles') AS section " +
        " where lls.is_delete is false and section->>'name' IS NOT null and s.learning_library_id = :learningLibraryId ", nativeQuery = true)
    Integer numDownloadsInLearningLibrary(UUID learningLibraryId);
}
