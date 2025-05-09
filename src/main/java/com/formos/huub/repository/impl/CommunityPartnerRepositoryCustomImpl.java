package com.formos.huub.repository.impl;

import com.formos.huub.domain.entity.CommunityPartner;
import com.formos.huub.domain.request.RequestSearchCommunityPartner;
import com.formos.huub.domain.response.communitypartner.ResponseSearchCommunityPartner;
import com.formos.huub.repository.CommunityPartnerRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.util.ObjectUtils;

import java.util.Objects;

public class CommunityPartnerRepositoryCustomImpl implements CommunityPartnerRepositoryCustom {

    private final EntityManager em;

    @Autowired
    public CommunityPartnerRepositoryCustomImpl(JpaContext context) {
        this.em = context.getEntityManagerByManagedType(CommunityPartner.class);
    }

    @Override
    public Page<ResponseSearchCommunityPartner> searchByTermAndCondition(RequestSearchCommunityPartner condition, Pageable pageable) {
        StringBuffer sql = new StringBuffer();
        StringBuilder sqlCount = new StringBuilder("select count(DISTINCT cp.id) ");
        StringBuilder sqlSelect = new StringBuilder("SELECT DISTINCT new com.formos.huub.domain.response.communitypartner.ResponseSearchCommunityPartner(cp.id AS id, " +
            " cp.name AS name, cp.email AS email, cp.isVendor AS isVendor, cp.status AS status, l.name AS state, cp.city AS city) ");
        sql.append(" FROM CommunityPartner cp " +
            "LEFT JOIN Location l ON cp.state = l.code AND l.locationType = 'STATE' " +
            " LEFT JOIN cp.portals p WHERE (1 = 1) ");
        if (Objects.nonNull(condition.getPortalId())) {
            sql.append(" AND (p.id = :portalId)");
        }
        if (!ObjectUtils.isEmpty(condition.getSearchConditions())){
            sql.append(" AND ");
            var sqlCondition = SqlQueryBuilder.generateSqlQuery(condition.getSearchConditions());
            sql.append(sqlCondition);
        }
        sqlCount.append(sql);
        sqlSelect.append(sql);
        sqlSelect.append(" ORDER BY ").append(pageable.getSort().toString().replace(":", ""));
        Query queryCount = em.createQuery(sqlCount.toString());
        Query querySelect = em.createQuery(sqlSelect.toString());
        if (Objects.nonNull(condition.getPortalId())) {
            queryCount.setParameter("portalId", condition.getPortalId());
            querySelect.setParameter("portalId", condition.getPortalId());
        }
        long total = (Long) queryCount.getSingleResult();
        querySelect.setFirstResult((pageable.getPageNumber()) * pageable.getPageSize());
        querySelect.setMaxResults(pageable.getPageSize());
        var results = querySelect.getResultList();
        return new PageImpl<>(results, pageable, total);
    }
}
