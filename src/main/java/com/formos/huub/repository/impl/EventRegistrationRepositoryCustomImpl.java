package com.formos.huub.repository.impl;

import com.formos.huub.domain.entity.EventRegistration;
import com.formos.huub.domain.request.businessowner.RequestSearchEventRegistrations;
import com.formos.huub.domain.response.eventregistration.ResponseSearchEventRegistration;
import com.formos.huub.framework.utils.ObjectUtils;
import com.formos.huub.repository.EventRegistrationRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaContext;

import java.util.Objects;

public class EventRegistrationRepositoryCustomImpl implements EventRegistrationRepositoryCustom {

    private final EntityManager em;

    @Autowired
    public EventRegistrationRepositoryCustomImpl(JpaContext context) {
        this.em = context.getEntityManagerByManagedType(EventRegistration.class);
    }
    @Override
    public Page<ResponseSearchEventRegistration> searchByTermAndCondition(RequestSearchEventRegistrations condition, Pageable pageable) {
        StringBuilder sql = new StringBuilder();
        StringBuilder sqlCount = new StringBuilder("select count(DISTINCT er.id) ");
        StringBuilder sqlSelect = new StringBuilder(
            "select  " +
                "er.id as id, er.registration_date as registrationDate, ce.start_time as startTime, ce.subject as subject, " +
                "u.normalized_full_name as businessOwnerName, p.platform_name as portalName "
        );
        sql.append(
                "from event_registrations er  " +
                "inner join calendar_event ce on er.calendar_event_id  = ce.id and ce.is_delete is false and er.is_delete is false " +
                "inner join  business_owner bo on bo.id = er.business_owner_id  " +
                "inner join jhi_user u on u.id = bo.user_id  " +
                "left join portal p on p.id = bo.portal_id where bo.is_delete is false  "
        );
        var portalId = condition.getPortalId();
        if (Objects.nonNull(portalId)){
                sql.append(" AND (bo.portal_id = :portalId) ");
        }
        if (!ObjectUtils.isEmpty(condition.getSearchKeyword())) {
            sql.append(
                " AND (LOWER(ce.subject) ilike concat('%', :searchKeyword, '%')  OR LOWER(u.normalized_full_name) ilike concat('%', :searchKeyword, '%') )"
            );
        }
        if (!ObjectUtils.isEmpty(condition.getSearchConditions())) {
            sql.append(" AND ");
            var sqlCondition = SqlQueryBuilder.generateSqlQuery(condition.getSearchConditions());
            sql.append(sqlCondition);
        }
        sqlCount.append(sql);
        sqlSelect.append(sql);
        sqlSelect.append(" ORDER BY ").append(pageable.getSort().toString().replace(":", ""));
        Query queryCount = em.createNativeQuery(sqlCount.toString());
        Query querySelect = em.createNativeQuery(sqlSelect.toString(), "search_event_registration_managements");
        if (Objects.nonNull(portalId)) {
            queryCount.setParameter("portalId", portalId);
            querySelect.setParameter("portalId", portalId);
        }
        if (!ObjectUtils.isEmpty(condition.getSearchKeyword())) {
            queryCount.setParameter("searchKeyword", condition.getSearchKeyword());
            querySelect.setParameter("searchKeyword", condition.getSearchKeyword());
        }
        long total = (Long) queryCount.getSingleResult();
        querySelect.setFirstResult((pageable.getPageNumber()) * pageable.getPageSize());
        querySelect.setMaxResults(pageable.getPageSize());
        var results = querySelect.getResultList();
        return new PageImpl<>(results, pageable, total);
    }
}
