package com.formos.huub.repository.impl;

import com.formos.huub.domain.entity.Portal;
import com.formos.huub.domain.request.businessowner.RequestSearchBusinessOwnerSurveys;
import com.formos.huub.domain.request.common.SearchConditionRequest;
import com.formos.huub.domain.request.survey.RequestSearchSurveyResponses;
import com.formos.huub.domain.response.survey.ResponseSearchSurveyResponses;
import com.formos.huub.repository.SurveyResponsesRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.util.ObjectUtils;

public class SurveyResponsesRepositoryCustomImpl implements SurveyResponsesRepositoryCustom {

    private final EntityManager em;

    @Autowired
    public SurveyResponsesRepositoryCustomImpl(JpaContext context) {
        this.em = context.getEntityManagerByManagedType(Portal.class);
    }

    @Override
    public Page<ResponseSearchSurveyResponses> searchSurveyResponsesByTermAndCondition(
        UUID surveyId,
        RequestSearchSurveyResponses request,
        Pageable pageable
    ) {
        // Construct the base SQL query
        String sqlBase = buildBaseSqlQuery(request);
        String sqlCondition = buildSqlCondition(request);
        // Generate SQL for counting and selecting the results
        String sqlCount = "SELECT COUNT(DISTINCT id) FROM (" + sqlBase + ") as temp " + sqlCondition;
        String sqlSelect =
            "SELECT * FROM (" + sqlBase + ") as temp " + sqlCondition + " ORDER BY " + pageable.getSort().toString().replace(":", "");
        // Execute the queries
        long total = executeCountQuery(sqlCount, surveyId);
        List<ResponseSearchSurveyResponses> results = executeSelectQuery(sqlSelect, surveyId, pageable);
        return new PageImpl<>(results, pageable, total);
    }

    @Override
    public Page<ResponseSearchSurveyResponses> searchSurveysResponsesByUserId(
        UUID userId,
        RequestSearchBusinessOwnerSurveys request,
        Pageable pageable
    ) {
        // Construct the base SQL query
        String sqlBase = buildBaseSqlQuery(request);
        String sqlCondition = buildSqlCondition(request);
        // Generate SQL for counting and selecting the results
        String sqlCount = "SELECT COUNT(DISTINCT id) FROM (" + sqlBase + ") as temp " + sqlCondition;
        String sqlSelect =
            "SELECT * FROM (" + sqlBase + ") as temp " + sqlCondition + " ORDER BY " + pageable.getSort().toString().replace(":", "");
        // Execute the queries
        long total = executeCountQuery(sqlCount, userId);
        List<ResponseSearchSurveyResponses> results = executeSelectQuery(sqlSelect, userId, pageable);
        return new PageImpl<>(results, pageable, total);
    }

    // Helper method to build the base SQL query dynamically
    private String buildBaseSqlQuery(Object request) {
        StringBuilder sql = new StringBuilder();
        sql.append(
            " SELECT sr.id AS id, sr.submission_date as submissionDate, u.normalized_full_name as fullName, ua.authority_name as role, sr.pdf_url as pdfUrl, u.id as userId, s.name as surveyName "
        );
        sql.append(" FROM survey_responses sr ");
        sql.append(" JOIN survey s ON s.id = sr.survey_id ");
        sql.append(" JOIN jhi_user u ON u.id = sr.user_id ");
        sql.append(" JOIN jhi_user_authority ua ON u.id = ua.user_id ");

        // Apply search conditions based on the request type
        if (request instanceof RequestSearchSurveyResponses) {
            sql.append(" WHERE sr.survey_id = :id ");
        } else if (request instanceof RequestSearchBusinessOwnerSurveys) {
            sql.append(" WHERE sr.user_id = :id ");
        } else {
            sql.append(" WHERE ( 1 = 1 ) ");
        }
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
    private List<ResponseSearchSurveyResponses> executeSelectQuery(String sql, UUID id, Pageable pageable) {
        Query querySelect = em.createNativeQuery(sql, "search_survey_responses");
        querySelect.setParameter("id", id);
        querySelect.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        querySelect.setMaxResults(pageable.getPageSize());
        return querySelect.getResultList();
    }
}
