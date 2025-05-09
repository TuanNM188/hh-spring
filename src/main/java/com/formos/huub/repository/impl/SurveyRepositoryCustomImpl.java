package com.formos.huub.repository.impl;

import com.formos.huub.domain.entity.Portal;
import com.formos.huub.domain.request.survey.RequestSearchSurvey;
import com.formos.huub.domain.response.survey.ResponseSearchSurvey;
import com.formos.huub.repository.SurveyRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.util.ObjectUtils;

public class SurveyRepositoryCustomImpl implements SurveyRepositoryCustom {

    private final EntityManager em;

    @Autowired
    public SurveyRepositoryCustomImpl(JpaContext context) {
        this.em = context.getEntityManagerByManagedType(Portal.class);
    }

    @Override
    public Page<ResponseSearchSurvey> searchSurveyByTermAndCondition(RequestSearchSurvey request, Pageable pageable) {
        StringBuffer sql = new StringBuffer();
        StringBuilder sqlCount = new StringBuilder("SELECT COUNT(DISTINCT id) ");
        StringBuilder sqlSelect = new StringBuilder("SELECT * ");
        sql.append(
            " FROM ( SELECT " +
            " s.id, s.name, s.description, s.is_active as isActive, p.platform_name as portalName, COUNT(sr.id) AS responses, p.url as portalUrl,  p.is_custom_domain as isCustomDomain, " +
            " s.created_date as createdDate, p.id as portalId " +
            " FROM survey s " +
            " JOIN portal p ON p.id = s.portal_id " +
            " LEFT JOIN survey_responses sr ON s.id = sr.survey_id " +
            " WHERE (1 = 1) "
        );
        if (Objects.nonNull(request.getPortalId())) {
            sql.append(" AND (p.id = :portalId)");
        }
        sql.append(" GROUP BY s.id, p.platform_name, p.id) as temp ");
        if (!ObjectUtils.isEmpty(request.getSearchConditions())) {
            sql.append(" WHERE ");
            var sqlCondition = SqlQueryBuilder.generateSqlQuery(request.getSearchConditions());
            sql.append(sqlCondition);
        }
        sqlCount.append(sql);
        sqlSelect.append(sql);
        sqlSelect.append(" ORDER BY ").append(pageable.getSort().toString().replace(":", ""));
        Query queryCount = em.createNativeQuery(sqlCount.toString());
        Query querySelect = em.createNativeQuery(sqlSelect.toString(), "search_survey");
        if (Objects.nonNull(request.getPortalId())) {
            queryCount.setParameter("portalId", request.getPortalId());
            querySelect.setParameter("portalId", request.getPortalId());
        }
        long total = (Long) queryCount.getSingleResult();
        querySelect.setFirstResult((pageable.getPageNumber()) * pageable.getPageSize());
        querySelect.setMaxResults(pageable.getPageSize());
        var results = querySelect.getResultList();
        return new PageImpl<>(results, pageable, total);
    }
}
