package com.formos.huub.repository.impl;

import com.formos.huub.domain.entity.CommunityPartner;
import com.formos.huub.domain.request.businessowner.RequestSearchEventRegistrations;
import com.formos.huub.domain.response.businessowner.ResponseSearchEventRegistrations;
import com.formos.huub.domain.response.calendarevent.RequestSearchCalendarEvents;
import com.formos.huub.domain.response.calendarevent.ResponseSearchCalendarEvents;
import com.formos.huub.framework.utils.StringUtils;
import com.formos.huub.repository.CalendarEventRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.Objects;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.util.ObjectUtils;

public class CalendarEventRepositoryCustomImpl implements CalendarEventRepositoryCustom {

    private final EntityManager em;

    @Autowired
    public CalendarEventRepositoryCustomImpl(JpaContext context) {
        this.em = context.getEntityManagerByManagedType(CommunityPartner.class);
    }

    @Override
    public Page<ResponseSearchCalendarEvents> searchByTermAndCondition(RequestSearchCalendarEvents condition, Pageable pageable) {
        StringBuilder sql = new StringBuilder();
        StringBuilder sqlCount = new StringBuilder("select count(distinct ce.id) ");
        StringBuilder sqlSelect = new StringBuilder(
            "SELECT distinct ce.id AS id, " +
            " ce.subject AS subject, ce.start_time AS startTime, ce.end_time AS endTime, (CASE WHEN ce.init_by = 'iCal' OR ce.init_by = 'Eventbrite' THEN ce.init_by ELSE u.normalized_full_name END) AS createdBy," +
            " ce.status AS status, ce.image_url as imageUrl, ce.website as website, ce.description as description, ce.init_by as initBy, ce.created_date as createdDate"
        );
        sql.append(
            " FROM calendar_event ce left join jhi_user u On u.login = ce.created_by " +
            " join portal_calendar_event p on p.calendar_event_id = ce.id WHERE (ce.status != 'DELETED') "
        );
        if (Objects.nonNull(condition.getPortalId())) {
            sql.append(" AND (p.portal_id = :portalId)");
        }

        if (Objects.nonNull(condition.getSearchKeyword())) {
            sql.append(
                " AND (ce.subject ilike concat('%',:searchKeyword,'%') " +
                "OR TRIM(BOTH ' ' FROM REGEXP_REPLACE(ce.description, :regexHtml, ' ', 'g')) ilike concat('%',:searchKeyword,'%'))"
            );
        }

        if (Objects.nonNull(condition.getStartDate()) && Objects.isNull(condition.getEndDate())) {
            sql.append(" AND TO_CHAR( ce.start_time AT TIME ZONE 'UTC' AT TIME ZONE :timezone, 'YYYY-MM-DD') = :startDate");
        }

        if (Objects.nonNull(condition.getStartDate()) && Objects.nonNull(condition.getEndDate())) {
            sql.append(
                " AND TO_CHAR( ce.start_time AT TIME ZONE 'UTC' AT TIME ZONE :timezone, 'YYYY-MM-DD') between :startDate and :endDate"
            );
        }

        if (Objects.nonNull(condition.getSearchMonth())) {
            sql.append(" AND TO_CHAR( ce.start_time, 'YYYY-MM') = :searchMonth");
        }

        if (Objects.nonNull(condition.getCurrentDate())) {
            sql.append(" AND TO_CHAR(ce.start_time, 'YYYY-MM-DD') >= :currentDate");
        }

        if (Objects.nonNull(condition.getStatus())) {
            sql.append(" AND ce.status = :status");
        }

        if (Objects.nonNull(condition.getOrganizerName())) {
            sql.append(" AND lower(ce.organizer_name) = lower(:organizerName)");
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
        Query querySelect = em.createNativeQuery(sqlSelect.toString(), "search_calendar_event");
        if (Objects.nonNull(condition.getPortalId())) {
            queryCount.setParameter("portalId", condition.getPortalId());
            querySelect.setParameter("portalId", condition.getPortalId());
        }
        if (Objects.nonNull(condition.getSearchKeyword())) {
            String wildcards = StringUtils.wildcards(condition.getSearchKeyword());
            queryCount.setParameter("searchKeyword", wildcards);
            querySelect.setParameter("searchKeyword", wildcards);
            queryCount.setParameter("regexHtml", condition.getRegexHtml());
            querySelect.setParameter("regexHtml", condition.getRegexHtml());
        }
        if (Objects.nonNull(condition.getStartDate()) && Objects.isNull(condition.getEndDate())) {
            queryCount.setParameter("startDate", condition.getStartDate());
            querySelect.setParameter("startDate", condition.getStartDate());
            queryCount.setParameter("timezone", condition.getTimezone());
            querySelect.setParameter("timezone", condition.getTimezone());
        }
        if (Objects.nonNull(condition.getStartDate()) && Objects.nonNull(condition.getEndDate())) {
            queryCount.setParameter("startDate", condition.getStartDate());
            querySelect.setParameter("startDate", condition.getStartDate());
            queryCount.setParameter("endDate", condition.getEndDate());
            querySelect.setParameter("endDate", condition.getEndDate());
            queryCount.setParameter("timezone", condition.getTimezone());
            querySelect.setParameter("timezone", condition.getTimezone());
        }
        if (Objects.nonNull(condition.getSearchMonth())) {
            queryCount.setParameter("searchMonth", condition.getSearchMonth());
            querySelect.setParameter("searchMonth", condition.getSearchMonth());
        }
        if (Objects.nonNull(condition.getStatus())) {
            queryCount.setParameter("status", condition.getStatus());
            querySelect.setParameter("status", condition.getStatus());
        }
        if (Objects.nonNull(condition.getOrganizerName())) {
            queryCount.setParameter("organizerName", condition.getOrganizerName());
            querySelect.setParameter("organizerName", condition.getOrganizerName());
        }

        if (Objects.nonNull(condition.getCurrentDate())) {
            queryCount.setParameter("currentDate", condition.getCurrentDate());
            querySelect.setParameter("currentDate", condition.getCurrentDate());
        }
        long total = (Long) queryCount.getSingleResult();
        querySelect.setFirstResult((pageable.getPageNumber()) * pageable.getPageSize());
        querySelect.setMaxResults(pageable.getPageSize());
        var results = querySelect.getResultList();
        return new PageImpl<>(results, pageable, total);
    }

    @Override
    public Page<ResponseSearchEventRegistrations> searchEventRegistrations(
        UUID bussinessOwnerId,
        RequestSearchEventRegistrations request,
        Pageable pageable
    ) {
        StringBuffer sql = new StringBuffer();
        StringBuilder sqlCount = new StringBuilder("SELECT COUNT(DISTINCT calendarEventId) ");
        StringBuilder sqlSelect = new StringBuilder("SELECT * ");
        sql.append(
            " FROM ( SELECT " +
            " DISTINCT ce.id as calendarEventId" +
            " , ce.subject as eventName" +
            " , date(ce.start_time::timestamp AT TIME ZONE 'UTC' AT TIME ZONE ce.timezone) as eventDate" +
            " FROM event_registrations er " +
            " JOIN business_owner bo ON er.business_owner_id = bo.id and bo.is_delete = false" +
            " JOIN calendar_event ce ON er.calendar_event_id = ce.id and ce.is_delete = false" +
            " JOIN portal_calendar_event pce ON pce.calendar_event_id = ce.id " +
            " WHERE er.is_delete = false "
        );
        if (Objects.nonNull(bussinessOwnerId)) {
            sql.append(" AND bo.id = :bussinessOwnerId");
        }
        sql.append(") as temp ");
        if (!ObjectUtils.isEmpty(request.getSearchConditions())) {
            sql.append(" WHERE ");
            var sqlCondition = SqlQueryBuilder.generateSqlQuery(request.getSearchConditions());
            sql.append(sqlCondition);
        }
        sqlCount.append(sql);
        sqlSelect.append(sql);
        sqlSelect.append(" ORDER BY ").append(pageable.getSort().toString().replace(":", ""));
        Query queryCount = em.createNativeQuery(sqlCount.toString());
        Query querySelect = em.createNativeQuery(sqlSelect.toString(), "search_event_registrations");
        if (Objects.nonNull(bussinessOwnerId)) {
            queryCount.setParameter("bussinessOwnerId", bussinessOwnerId);
            querySelect.setParameter("bussinessOwnerId", bussinessOwnerId);
        }
        long total = (Long) queryCount.getSingleResult();
        querySelect.setFirstResult((pageable.getPageNumber()) * pageable.getPageSize());
        querySelect.setMaxResults(pageable.getPageSize());
        var results = querySelect.getResultList();
        return new PageImpl<>(results, pageable, total);
    }
}
