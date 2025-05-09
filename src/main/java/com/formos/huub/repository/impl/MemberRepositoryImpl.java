package com.formos.huub.repository.impl;

import static com.formos.huub.repository.impl.SqlQueryBuilder.generateSqlQuery;

import com.formos.huub.domain.request.member.RequestSearchMember;
import com.formos.huub.domain.response.member.ResponseSearchMember;
import com.formos.huub.repository.MemberRepositoryCustom;
import com.formos.huub.security.AuthoritiesConstants;
import com.formos.huub.security.SecurityUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

@Repository
public class MemberRepositoryImpl implements MemberRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<ResponseSearchMember> getAllMemberWithPageable(RequestSearchMember request, UUID portalId, Pageable pageable) {
        StringBuffer sql = new StringBuffer();
        StringBuilder sqlCount = new StringBuilder("SELECT count(DISTINCT u.id) ");

        StringBuilder sqlSelect = new StringBuilder(
            "SELECT new com.formos.huub.domain.response.member.ResponseSearchMember(u.id AS id, " +
            " u.normalizedFullName AS normalizedFullName, u.email AS email, au.name AS role, u.status AS status, ph.isPrimary AS isPortalHostPrimary, u.username as username) "
        );
        sql.append(" FROM User u JOIN u.authorities au LEFT JOIN PortalHost ph ON u.id = ph.userId ");
        sql.append(" LEFT JOIN ph.portal php ");
        sql.append(" LEFT JOIN u.technicalAdvisor.portals pta ");
        sql.append(" LEFT JOIN u.technicalAdvisor.communityPartner cpta  ");
        sql.append(" LEFT JOIN CommunityPartner cp on cp.id = u.communityPartner.id left join cp.portals pcp ");
        sql.append(" LEFT JOIN BusinessOwner bo on bo.user.id = u.id left join bo.portal pbo ");
        sql.append(" WHERE (1 = 1)");
        if (Objects.nonNull(portalId)) {
            sql.append(" AND (php.id = :portalId OR pta.id = :portalId OR pcp.id = :portalId OR pbo.id = :portalId)");
        }
        if (Objects.nonNull(request.getCommunityPartnerId())) {
            sql.append(" AND (cpta.id = :communityPartnerId OR cp.id = :communityPartnerId)");
        }
        if (!ObjectUtils.isEmpty(request.getSearchConditions())) {
            sql.append(" AND ");
            var sqlCondition = generateSqlQuery(request.getSearchConditions());
            sql.append(sqlCondition);
        }
        sqlCount.append(sql);
        sqlSelect.append(sql);
        sqlSelect.append(" GROUP BY u.id, au.name, ph.isPrimary ");
        sqlSelect.append(" ORDER BY ").append(pageable.getSort().toString().replace(":", ""));
        TypedQuery<ResponseSearchMember> querySelect = em.createQuery(sqlSelect.toString(), ResponseSearchMember.class);
        TypedQuery<Long> queryCount = em.createQuery(sqlCount.toString(), Long.class);
        if (Objects.nonNull(portalId)) {
            queryCount.setParameter("portalId", portalId);
            querySelect.setParameter("portalId", portalId);
        }
        if (Objects.nonNull(request.getCommunityPartnerId())) {
            queryCount.setParameter("communityPartnerId", request.getCommunityPartnerId());
            querySelect.setParameter("communityPartnerId", request.getCommunityPartnerId());
        }
        querySelect.setFirstResult((pageable.getPageNumber()) * pageable.getPageSize());
        querySelect.setMaxResults(pageable.getPageSize());

        List<ResponseSearchMember> members = querySelect.getResultList();
        long total = queryCount.getSingleResult();

        return new PageImpl<>(members, pageable, total);
    }
}
