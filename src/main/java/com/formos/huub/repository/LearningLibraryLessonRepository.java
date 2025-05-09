package com.formos.huub.repository;

import com.formos.huub.domain.entity.LearningLibraryLesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LearningLibraryLessonRepository extends JpaRepository<LearningLibraryLesson, UUID> {

    @Query("SELECT COUNT(l) FROM LearningLibraryLesson l WHERE l.learningLibraryStep.learningLibrary.id = :learningLibraryId")
    int countLessonInCourseById(UUID learningLibraryId);
}
