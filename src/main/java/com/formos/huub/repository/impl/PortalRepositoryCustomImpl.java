package com.formos.huub.repository.impl;

import com.formos.huub.domain.entity.Portal;
import com.formos.huub.domain.request.portals.RequestSearchPortals;
import com.formos.huub.domain.response.portals.SearchPortalsResponse;
import com.formos.huub.repository.PortalRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.util.ObjectUtils;

public class PortalRepositoryCustomImpl implements PortalRepositoryCustom {

    private final EntityManager em;

    @Autowired
    public PortalRepositoryCustomImpl(JpaContext context) {
        this.em = context.getEntityManagerByManagedType(Portal.class);
    }

    @Override
    public Page<SearchPortalsResponse> searchByTermAndCondition(RequestSearchPortals searchTerm, Pageable pageable) {

        StringBuffer sql = new StringBuffer();
        StringBuilder sqlCount = new StringBuilder("select count(p.id) ");
        StringBuilder sqlSelect = new StringBuilder("SELECT new com.formos.huub.domain.response.portals.SearchPortalsResponse(p.id AS id, " +
            " p.platformName AS platformName, p.regionName AS regionName, ls.name AS state, p.city AS city, p.status AS status, " +
            " pr.contractStart AS contractYearStartDate, pr.contractEnd AS contractYearEndDate) ");
        sql.append(" FROM Portal p left join Program pr ON pr.portal.id = p.id ");
        sql.append(" left join Location ls on p.state = ls.code and ls.locationType = 'STATE' ");
        if (!ObjectUtils.isEmpty(searchTerm.getSearchConditions())){
            sql.append(" WHERE ");
            var sqlCondition = SqlQueryBuilder.generateSqlQuery(searchTerm.getSearchConditions());
            sql.append(sqlCondition);
        }
        sqlCount.append(sql);
        sqlSelect.append(sql);
        sqlSelect.append(" ORDER BY ").append(pageable.getSort().toString().replace(":", ""));
        Query queryCount = em.createQuery(sqlCount.toString());
        Query querySelect = em.createQuery(sqlSelect.toString());
        long total = (Long) queryCount.getSingleResult();
        querySelect.setFirstResult((pageable.getPageNumber()) * pageable.getPageSize());
        querySelect.setMaxResults(pageable.getPageSize());
        var results = querySelect.getResultList();
        return new PageImpl<>(results, pageable, total);

    }
}
