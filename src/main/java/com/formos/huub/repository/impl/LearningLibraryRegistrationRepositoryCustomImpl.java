package com.formos.huub.repository.impl;

import com.formos.huub.domain.request.Learninglibraryregistration.RequestSearchLearningLibraryRegistration;
import com.formos.huub.domain.request.Learninglibraryregistration.RequestSearchLessonSurvey;
import com.formos.huub.domain.request.businessowner.RequestSearchCourseRegistrations;
import com.formos.huub.domain.request.businessowner.RequestSearchCourseSurveys;
import com.formos.huub.domain.request.common.SearchConditionRequest;
import com.formos.huub.domain.response.businessowner.ResponseSearchCourseRegistrations;
import com.formos.huub.domain.response.learninglibraryregistration.ResponseSearchLearningLibraryRegistration;
import com.formos.huub.domain.response.learninglibraryregistration.ResponseSearchLessonSurvey;
import com.formos.huub.repository.LearningLibraryRegistrationRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class LearningLibraryRegistrationRepositoryCustomImpl implements LearningLibraryRegistrationRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<ResponseSearchLearningLibraryRegistration> searchRegistrationByTermAndCondition(
        RequestSearchLearningLibraryRegistration request,
        UUID portalId,
        Pageable pageable
    ) {
        StringBuffer sql = new StringBuffer();
        StringBuilder sqlCount = new StringBuilder("SELECT COUNT(DISTINCT llr.id) ");
        StringBuilder sqlSelect = new StringBuilder(
            "SELECT DISTINCT llr.id AS id, u.normalized_full_name AS businessOwnerName, ll.name AS courseName, " +
            "llr.registration_date AS submissionDate, llr.registration_status AS status, p.platform_name as portalName "
        );
        sql.append(
            "FROM learning_library_registration llr " +
            "JOIN jhi_user u on u.id = llr.user_id " +
            "JOIN learning_library ll on ll.id = llr.learning_library_id " +
            "LEFT JOIN learning_library_portal llp on llp.learning_library_id = ll.id " +
            "LEFT JOIN portal p on llp.portal_id = p.id " +
            "WHERE llr.is_delete is false "
        );
        if (Objects.nonNull(portalId)) {
            sql.append(" AND (llp.portal_id = :portalId) ");
        }
        if (Objects.nonNull(request.getSearchKeyword())) {
            sql.append(
                " AND (u.normalized_full_name ILIKE concat('%', :searchKeyword, '%') OR ll.name ILIKE concat('%', :searchKeyword, '%') " +
                    "OR p.platform_name ILIKE concat('%', :searchKeyword, '%'))"
            );
        }
        if (!ObjectUtils.isEmpty(request.getSearchConditions())) {
            sql.append(" AND ");
            var sqlCondition = SqlQueryBuilder.generateSqlQuery(request.getSearchConditions());
            sql.append(sqlCondition);
        }
        sqlCount.append(sql);
        sqlSelect.append(sql);
        sqlSelect.append(" ORDER BY ").append(pageable.getSort().toString().replace(":", ""));

        Query querySelect = em.createNativeQuery(
            sqlSelect.toString(),
            "search_learning_library_registrations"
        );
        Query queryCount = em.createNativeQuery(sqlCount.toString());
        if (Objects.nonNull(portalId)) {
            queryCount.setParameter("portalId", portalId);
            querySelect.setParameter("portalId", portalId);
        }
        if (Objects.nonNull(request.getSearchKeyword())) {
            queryCount.setParameter("searchKeyword", request.getSearchKeyword());
            querySelect.setParameter("searchKeyword", request.getSearchKeyword());
        }

        querySelect.setFirstResult((pageable.getPageNumber()) * pageable.getPageSize());
        querySelect.setMaxResults(pageable.getPageSize());
        long total = (Long) queryCount.getSingleResult();
        querySelect.setFirstResult((pageable.getPageNumber()) * pageable.getPageSize());
        querySelect.setMaxResults(pageable.getPageSize());
        var results = querySelect.getResultList();
        return new PageImpl<>(results, pageable, total);
    }

    @Override
    public Page<ResponseSearchLessonSurvey> searchLessonSurveyByTermAndCondition(
        RequestSearchLessonSurvey request,
        UUID portalId,
        Pageable pageable
    ) {
        StringBuffer sql = new StringBuffer();
        StringBuilder sqlCount = new StringBuilder("SELECT COUNT(DISTINCT llrd.id) ");
        StringBuilder sqlSelect = new StringBuilder(
            "SELECT DISTINCT " +
            "llrd.id AS id, u.normalized_full_name AS businessOwnerName, ll.name AS courseName, lll.title AS lessonName, llrd.submission_date AS submissionDate, " +
            "llrd.survey_data as surveyData, lls.survey_title as surveyName, lls.survey_description as surveyDescription, p.platform_name as portalName "
        );
        sql.append(
            "FROM learning_library_registration_detail llrd " +
            "JOIN learning_library_registration llr on llrd.learning_library_registration_id = llr.id " +
            "JOIN jhi_user u on llr.user_id = u.id " +
            "JOIN learning_library ll on llr.learning_library_id = ll.id " +
            "JOIN learning_library_lesson lll ON llrd.lesson_id = lll.id " +
            "JOIN learning_library_section lls ON llrd.lesson_id = lls.learning_library_lesson_id and lls.section_type = 'SURVEY' " +
            "LEFT JOIN learning_library_portal llp on llp.learning_library_id = ll.id " +
            "LEFT JOIN portal p on llp.portal_id = p.id " +
            "WHERE llrd.survey_data IS NOT null "
        );
        if (Objects.nonNull(portalId)) {
            sql.append(" AND (llp.portal_id = :portalId) ");
        }
        if (Objects.nonNull(request.getSearchKeyword())) {
            sql.append(
                " AND (u.normalized_full_name ILIKE concat('%',:searchKeyword,'%') " +
                    "OR ll.name ILIKE concat('%',:searchKeyword,'%') OR lll.title ILIKE concat('%',:searchKeyword,'%')" +
                    " OR p.platform_name ILIKE concat('%',:searchKeyword,'%'))"
            );
        }
        if (!ObjectUtils.isEmpty(request.getSearchConditions())) {
            sql.append(" AND ");
            var sqlCondition = SqlQueryBuilder.generateSqlQuery(request.getSearchConditions());
            sql.append(sqlCondition);
        }
        sqlCount.append(sql);
        sqlSelect.append(sql);
        sqlSelect.append(" ORDER BY ").append(pageable.getSort().toString().replace(":", ""));

        Query queryCount = em.createNativeQuery(sqlCount.toString());
        Query querySelect = em.createNativeQuery(sqlSelect.toString(), "search_lesson_surveys");
        if (Objects.nonNull(portalId)) {
            queryCount.setParameter("portalId", portalId);
            querySelect.setParameter("portalId", portalId);
        }
        if (Objects.nonNull(request.getSearchKeyword())) {
            queryCount.setParameter("searchKeyword", request.getSearchKeyword());
            querySelect.setParameter("searchKeyword", request.getSearchKeyword());
        }

        querySelect.setFirstResult((pageable.getPageNumber()) * pageable.getPageSize());
        querySelect.setMaxResults(pageable.getPageSize());
        long total = (Long) queryCount.getSingleResult();
        querySelect.setFirstResult((pageable.getPageNumber()) * pageable.getPageSize());
        querySelect.setMaxResults(pageable.getPageSize());
        var results = querySelect.getResultList();
        return new PageImpl<>(results, pageable, total);
    }

    @Override
    public Page<ResponseSearchCourseRegistrations> searchCourseRegistrations(
        UUID userId,
        RequestSearchCourseRegistrations request,
        Pageable pageable
    ) {
        StringBuffer sqlWith = new StringBuffer();
        sqlWith.append(
            " WITH learning_library_data AS (" +
            " SELECT " +
            " lb.id, lb.name, count(les.id) AS total_lessons, lb.category_id, lb.access_type" +
            " FROM learning_library lb " +
            " JOIN learning_library_step step ON step.learning_library_id = lb.id AND step.is_delete = false" +
            " JOIN learning_library_lesson les ON les.learning_library_step_id = step.id AND les.is_delete = false" +
            " WHERE lb.is_delete = false" +
            " GROUP BY lb.id" +
            " ),learning_library_registration_data AS (" +
            " SELECT reg.id ,count(rd.id) AS completed_lessons ,MAX(rd.completion_date) AS completion_date, MIN(rd.created_date) AS started_date" +
            " FROM learning_library_registration reg" +
            " JOIN learning_library_registration_detail rd ON rd.learning_library_registration_id = reg.id AND rd.learning_status='COMPLETE' AND rd.is_delete = false" +
            " WHERE reg.is_delete = false" +
            " GROUP BY reg.id" +
            " ) "
        );
        // Construct the base SQL query
        String sqlBase = buildBaseSqlQueryCourseRegistrations();
        String sqlCondition = buildSqlCondition(request);
        // Generate SQL for counting and selecting the results
        String sqlCount = sqlWith + " SELECT COUNT(DISTINCT id) FROM (" + sqlBase + ") as temp " + sqlCondition;
        String sqlSelect =
            sqlWith +
            " SELECT * FROM (" +
            sqlBase +
            ") as temp " +
            sqlCondition +
            " ORDER BY " +
            pageable.getSort().toString().replace(":", "");
        // Execute the queries
        long total = executeCountQuery(sqlCount, userId);
        List<ResponseSearchCourseRegistrations> results = (List<ResponseSearchCourseRegistrations>) executeSelectQuery(
            sqlSelect,
            userId,
            "search_course_registrations",
            pageable
        );
        return new PageImpl<>(results, pageable, total);
    }

    @Override
    public Page<ResponseSearchLessonSurvey> searchCourseSurveys(UUID userId, RequestSearchCourseSurveys request, Pageable pageable) {
        // Construct the base SQL query
        String sqlBase = buildBaseSqlQueryCourseSurveys();
        String sqlCondition = buildSqlCondition(request);
        // Generate SQL for counting and selecting the results
        String sqlCount = "SELECT COUNT(DISTINCT id) FROM (" + sqlBase + ") as temp " + sqlCondition;
        String sqlSelect =
            "SELECT * FROM (" + sqlBase + ") as temp " + sqlCondition + " ORDER BY " + pageable.getSort().toString().replace(":", "");
        // Execute the queries
        long total = executeCountQuery(sqlCount, userId);
        List<ResponseSearchLessonSurvey> results = (List<ResponseSearchLessonSurvey>) executeSelectQuery(
            sqlSelect,
            userId,
            "search_course_surveys",
            pageable
        );
        return new PageImpl<>(results, pageable, total);
    }

    // Helper method to build the base SQL query dynamically
    private String buildBaseSqlQueryCourseSurveys() {
        StringBuilder sql = new StringBuilder();
        sql.append(
            " SELECT DISTINCT " +
            "  llrd.id," +
            "  u.normalized_full_name as businessOwnerName," +
            "  ll.name as courseName," +
            "  lll.title as lessonName," +
            "  llrd.submission_date as submissionDate," +
            "  llrd.survey_data as surveyData," +
            "  lls.survey_title as surveyName," +
            "  lls.survey_description as surveyDescription" +
            " FROM learning_library_registration_detail llrd" +
            " JOIN learning_library_registration llr ON llr.id=llrd.learning_library_registration_id AND llr.is_delete=false" +
            " JOIN jhi_user u ON u.id=llr.user_id AND u.is_delete = false" +
            " JOIN learning_library ll ON ll.id=llr.learning_library_id AND ll.is_delete=false" +
            " JOIN learning_library_lesson lll ON lll.is_delete=false AND llrd.lesson_id=lll.id" +
            " JOIN learning_library_section lls ON lls.is_delete=false AND llrd.lesson_id=lls.learning_library_lesson_id AND lls.section_type='SURVEY'" +
            " WHERE llrd.is_delete=false AND llrd.survey_data IS NOT NULL and llr.user_id = :id"
        );
        return sql.toString();
    }

    private String buildBaseSqlQueryCourseRegistrations() {
        StringBuilder sql = new StringBuilder();
        sql.append(
            " SELECT " +
            "  cr.id" +
            " ,co.name " +
            " ,co.access_type AS accessType" +
            " ,cat.id AS categoryId" +
            " ,cat.name AS categoryName" +
            " ,CASE  WHEN co.access_type = 'CLOSED' THEN cr.registration_date ELSE NULL END AS enrolledDate" +
            " ,crd.started_date as startedDate" +
            " ,CASE  WHEN co.total_lessons = crd.completed_lessons THEN crd.completion_date ELSE NULL END AS completedDate" +
            " ,CASE  WHEN co.total_lessons = crd.completed_lessons THEN COALESCE(ul.rating, 0)  ELSE NULL END AS rating" +
            " ,COALESCE(crd.completed_lessons, 0) AS completedLesson" +
            " ,COALESCE(co.total_lessons, 0) AS totalLesson" +
            " FROM learning_library_registration cr" +
            " LEFT JOIN user_learning_library ul ON cr.user_id = ul.user_id AND cr.learning_library_id = ul.learning_library_id" +
            " JOIN learning_library_data co ON co.id = cr.learning_library_id" +
            " JOIN category cat ON cat.id = co.category_id" +
            " left JOIN learning_library_registration_data crd ON cr.id = crd.id" +
            " WHERE cr.is_delete = FALSE AND cr.user_id = :id"
        );
        return sql.toString();
    }

    // Helper method to build the base SQL condition dynamically
    private String buildSqlCondition(Object request) {
        StringBuilder sqlCondition = new StringBuilder();
        SearchConditionRequest r = (SearchConditionRequest) request;
        if (!ObjectUtils.isEmpty(r.getSearchConditions())) {
            sqlCondition.append(" WHERE ");
            sqlCondition.append(SqlQueryBuilder.generateSqlQuery(r.getSearchConditions()));
        }
        return sqlCondition.toString();
    }

    // Helper method to execute count query
    private long executeCountQuery(String sql, UUID id) {
        Query queryCount = em.createNativeQuery(sql);
        queryCount.setParameter("id", id);
        return (Long) queryCount.getSingleResult();
    }

    // Helper method to execute select query
    private List executeSelectQuery(String sql, UUID id, String mapperKey, Pageable pageable) {
        Query querySelect = em.createNativeQuery(sql, mapperKey);
        querySelect.setParameter("id", id);
        querySelect.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        querySelect.setMaxResults(pageable.getPageSize());
        return querySelect.getResultList();
    }
}
