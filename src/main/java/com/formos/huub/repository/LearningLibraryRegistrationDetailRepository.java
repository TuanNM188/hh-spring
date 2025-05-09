package com.formos.huub.repository;

import com.formos.huub.domain.entity.LearningLibraryRegistrationDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LearningLibraryRegistrationDetailRepository extends JpaRepository<LearningLibraryRegistrationDetail, UUID> {

    @Query("SELECT rd " +
        "FROM LearningLibraryRegistrationDetail rd " +
        "JOIN rd.learningLibraryRegistration r " +
        "WHERE r.user.id = :userId AND rd.lessonId = :lessonId"
    )
    Optional<LearningLibraryRegistrationDetail> findByUserAndLessonId(UUID userId, UUID lessonId);

    @Query("SELECT rd " +
        "FROM LearningLibraryRegistrationDetail rd " +
        "JOIN rd.learningLibraryRegistration r " +
        "WHERE r.user.id = :userId AND rd.lessonId IN (:lessonIds)"
    )
    List<LearningLibraryRegistrationDetail> findByUserAndLessonIdIn(UUID userId, List<UUID> lessonIds);

    @Query("SELECT COUNT(rd) " +
        "FROM LearningLibraryRegistrationDetail rd " +
        "JOIN rd.learningLibraryRegistration r " +
        "WHERE r.user.id = :userId AND r.learningLibrary.id = :learningLibraryId AND rd.learningStatus = 'COMPLETE'"
    )
    int countCompleteLessonByLearningLibraryId(UUID userId, UUID learningLibraryId);

}
