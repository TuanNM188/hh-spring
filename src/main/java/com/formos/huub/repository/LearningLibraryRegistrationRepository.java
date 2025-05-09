package com.formos.huub.repository;

import com.formos.huub.domain.entity.LearningLibraryRegistration;
import com.formos.huub.domain.response.learninglibraryregistration.IResponseDetailLessonSurvey;
import com.formos.huub.domain.response.learninglibraryregistration.IResponseDetailRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LearningLibraryRegistrationRepository extends JpaRepository<LearningLibraryRegistration, UUID>, LearningLibraryRegistrationRepositoryCustom {

    Optional<LearningLibraryRegistration> findByUserIdAndLearningLibraryId(UUID userId, UUID learningLibraryId);

    @Query("SELECT llr.id AS id, u.normalizedFullName AS businessOwnerName, u.phoneNumber AS phoneNumber, u.email AS email, ll.name AS courseName, " +
        "llr.registrationStatus AS registrationStatus, llr.decisionDate AS decisionDate, ll.enrolleeLimit AS enrolleeLimit, " +
        "ll.numberOfRegistered AS numberOfRegistered, llr.registrationDate AS registrationDate, ll.enrollmentDeadline AS enrollmentDeadline, " +
        "ll.startDate AS startDate, ll.endDate AS endDate, ll.surveyJson AS surveyJson, llr.surveyData AS surveyData, " +
        "ll.isRegistrationFormRequired AS isRegistrationFormRequired, u.id AS userId, llr.courseType AS courseType " +
        "FROM LearningLibraryRegistration llr " +
        "JOIN llr.learningLibrary ll " +
        "JOIN llr.user u " +
        "WHERE llr.id = :id"
    )
    IResponseDetailRegistration getRegistrationById(UUID id);

    @Query("SELECT llrd.id AS id, u.normalizedFullName AS businessOwnerName, u.phoneNumber AS phoneNumber, u.email AS email, ll.name AS courseName, " +
        "lll.title AS lessonName, llrd.submissionDate AS submissionDate, s.contents AS contents, llrd.surveyData AS surveyData, u.id AS userId " +
        "FROM LearningLibraryRegistrationDetail llrd " +
        "JOIN llrd.learningLibraryRegistration llr  " +
        "JOIN llr.learningLibrary ll " +
        "JOIN llr.user u " +
        "JOIN LearningLibraryLesson lll ON llrd.lessonId = lll.id " +
        "JOIN lll.sections s ON s.sectionType = 'SURVEY' " +
        "WHERE llrd.id = :id"
    )
    IResponseDetailLessonSurvey getLessonSurveyById(UUID id);

}
