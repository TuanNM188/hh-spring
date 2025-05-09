package com.formos.huub.repository.impl;

import com.formos.huub.domain.request.technicaladvisor.RequestListTechnicalAdvisor;
import com.formos.huub.domain.response.technicaladvisor.ResponseTechnicalAdvisor;
import com.formos.huub.repository.TechnicalAdvisorRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.formos.huub.repository.impl.SqlQueryBuilder.generateSqlQuery;

@Repository
public class TechnicalAdvisorRepositoryImpl implements TechnicalAdvisorRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<ResponseTechnicalAdvisor> getAllTechnicalAdvisorsWithPageable(RequestListTechnicalAdvisor request, UUID portalId, Pageable pageable) {
        StringBuffer sql = new StringBuffer();
        StringBuilder sqlCount = new StringBuilder("SELECT count(DISTINCT ta.id) ");
        StringBuilder sqlSelect = new StringBuilder("SELECT ta.id AS id, " +
            "u.normalized_full_name AS normalizedFullName, u.email AS email, u.organization AS organization, u.phone_number AS phoneNumber," +
            " u.status AS status, " +
            "ta.created_date AS startDate, u.id AS userId ");
        sql.append("FROM technical_advisor ta " +
            " INNER JOIN jhi_user u on u.id = ta.user_id" +
            " INNER JOIN jhi_user_authority au on au.user_id = u.id and au.authority_name = 'ROLE_TECHNICAL_ADVISOR' " +
            " LEFT JOIN technical_advisor_portal tap on ta.id = tap.technical_advisor_id " +
            " WHERE ta.is_delete is false and u.is_delete is false");

        if (Objects.nonNull(portalId)) {
            sql.append(" AND (tap.portal_id = :portalId) ");
        }

        if (!ObjectUtils.isEmpty(request.getSearchConditions())){
            sql.append(" AND ");
            var sqlCondition = generateSqlQuery(request.getSearchConditions());
            sql.append(sqlCondition);
        }

        sqlCount.append(sql);
        sqlSelect.append(sql);
        sqlSelect.append(" GROUP BY u.id, ta.id ");
        sqlSelect.append(" ORDER BY ").append(pageable.getSort().toString().replace(":", ""));

        Query queryCount = em.createNativeQuery(sqlCount.toString());
        Query querySelect = em.createNativeQuery(sqlSelect.toString(), "search_technical_advisors");
        if (Objects.nonNull(portalId)) {
            queryCount.setParameter("portalId", portalId);
            querySelect.setParameter("portalId", portalId);
        }

        long total = (Long) queryCount.getSingleResult();
        querySelect.setFirstResult((pageable.getPageNumber()) * pageable.getPageSize());
        querySelect.setMaxResults(pageable.getPageSize());
        var results = querySelect.getResultList();
        return new PageImpl<>(results, pageable, total);
    }

}
