package com.formos.huub.repository.impl;

import com.formos.huub.domain.entity.Portal;
import com.formos.huub.domain.request.common.SearchConditionRequest;
import com.formos.huub.domain.request.portals.RequestSearchPortalFunding;
import com.formos.huub.domain.request.portals.RequestSearchPortals;
import com.formos.huub.domain.response.portals.SearchPortalFundingResponse;
import com.formos.huub.repository.PortalFundingRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.util.ObjectUtils;

public class PortalFundingRepositoryCustomImpl implements PortalFundingRepositoryCustom {

    private final EntityManager em;

    @Autowired
    public PortalFundingRepositoryCustomImpl(JpaContext context) {
        this.em = context.getEntityManagerByManagedType(Portal.class);
    }

    @Override
    public Page<SearchPortalFundingResponse> searchByTermAndCondition(RequestSearchPortalFunding searchTerm, Pageable pageable) {
        StringBuffer sql = new StringBuffer();
        StringBuilder sqlCount = new StringBuilder("select count(f.id) ");
        StringBuilder sqlSelect = new StringBuilder("SELECT new com.formos.huub.domain.response.portals.SearchPortalFundingResponse(f.id AS id, " +
            " f.title AS title, f.amount AS amount, f.publishDate AS publishDate, f.applicationDeadline AS applicationDeadline, f.status AS status) ");
        sql.append(" FROM Funding f join f.portals l where l.id = :portalId ");
        if (!ObjectUtils.isEmpty(searchTerm.getSearchConditions())){
            sql.append(" AND ");
            var sqlCondition = SqlQueryBuilder.generateSqlQuery(searchTerm.getSearchConditions());
            sql.append(sqlCondition);
        }
        sqlCount.append(sql);
        sqlSelect.append(sql);
        sqlSelect.append(" ORDER BY ").append(pageable.getSort().toString().replace(":", ""));
        Query queryCount = em.createQuery(sqlCount.toString());
        Query querySelect = em.createQuery(sqlSelect.toString());

        queryCount.setParameter("portalId", searchTerm.getPortalId());
        querySelect.setParameter("portalId", searchTerm.getPortalId());
        long total = (Long) queryCount.getSingleResult();
        querySelect.setFirstResult((pageable.getPageNumber()) * pageable.getPageSize());
        querySelect.setMaxResults(pageable.getPageSize());
        var results = querySelect.getResultList();
        return new PageImpl<>(results, pageable, total);
    }
}
