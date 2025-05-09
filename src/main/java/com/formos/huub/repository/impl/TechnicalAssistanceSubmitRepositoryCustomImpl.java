package com.formos.huub.repository.impl;

import com.formos.huub.domain.request.technicalassistance.RequestSearchTechnicalApplicationSubmit;
import com.formos.huub.domain.response.technicalassistance.ResponseSearchApplication;
import com.formos.huub.framework.utils.StringUtils;
import com.formos.huub.repository.TechnicalAssistanceSubmitRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import java.util.Objects;

@Repository
public class TechnicalAssistanceSubmitRepositoryCustomImpl implements TechnicalAssistanceSubmitRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<ResponseSearchApplication> getAllApplicationProjectWithPageable(RequestSearchTechnicalApplicationSubmit searchTerm, Pageable pageable) {
        StringBuilder sql = new StringBuilder();
        StringBuilder sqlCount = new StringBuilder("select count(tas.id) ");
        StringBuilder sqlSelect = new StringBuilder("SELECT " +
            " tas.id AS id, null as projectId, u.id as userId, " +
            " u.normalized_full_name AS businessOwnerName, lastAp.lastAppointment AS lastAppointment, " +
            " upComing.upcomingAppointment AS upcomingAppointment, pro.projectStatus AS projectStatus," +
            " tas.status AS applicationStatus, tas.remaining_award_hours as remainingAwardHours, p.platform_name as platformName ");
        sql.append(" FROM technical_assistance_submit tas" +
            " left join program_term_vendor ptv on tas.assign_vendor_id = ptv.id" +
            " join program_term pt on tas.program_term_id = pt.id and pt.is_current is true" +
            " join portal p on tas.portal_id = p.id and tas.is_delete is false" +
            " join jhi_user u on tas.user_id = u.id and u.status = 'ACTIVE'" +
            " LEFT JOIN (SELECT tas.id as technicalAssistanceSubmitId, max(a.appointment_date) as lastAppointment from appointment a " +
            " Join technical_assistance_submit tas on a.technical_assistance_submit_id = tas.id and a.is_delete is false " +
            " WHERE a.appointment_date < NOW() group by tas.id) as lastAp on lastAp.technicalAssistanceSubmitId = tas.id " +
            " LEFT JOIN (SELECT tas.id as technicalAssistanceSubmitId, min(a.appointment_date) as upcomingAppointment from appointment a " +
            " Join technical_assistance_submit tas on a.technical_assistance_submit_id = tas.id and a.is_delete is false " +
            " WHERE a.appointment_date > NOW() group by tas.id) as upComing on upComing.technicalAssistanceSubmitId = tas.id " +
            " LEFT JOIN (SELECT tas.id as technicalAssistanceSubmitId, max(pr.status) as projectStatus from project pr " +
            "            Join technical_assistance_submit tas on pr.technical_assistance_submit_id = tas.id and pr.is_delete is false" +
            "            group by tas.id ) as pro on pro.technicalAssistanceSubmitId = tas.id " +
            " where 1 = 1");
        var portalIds = searchTerm.getPortalIds();
        if (!ObjectUtils.isEmpty(portalIds)) {
            sql.append(" AND p.id In (:portalIds)");
        }

        if (Objects.nonNull(searchTerm.getCommunityPartnerId())) {
            sql.append(" AND ptv.vendor_id = :communityPartnerId");
        }

        if (Objects.nonNull(searchTerm.getStatus())) {
            sql.append(" AND tas.status IN (SELECT UNNEST(string_to_array(:status, ',')))");
        }
        if (Objects.nonNull(searchTerm.getExcludeStatus())) {
            sql.append(" AND tas.status NOT IN (SELECT UNNEST(string_to_array(:excludeStatus, ',')))");
        }

        if (!ObjectUtils.isEmpty(searchTerm.getSearchKeyword())) {
            sql.append(" AND u.normalized_full_name ilike concat('%',:searchKeyword, '%') or p.platform_name ilike concat('%',:searchKeyword, '%')");
        }
        if (!ObjectUtils.isEmpty(searchTerm.getSearchConditions())) {
            sql.append(" AND ");
            var sqlCondition = SqlQueryBuilder.generateSqlQuery(searchTerm.getSearchConditions());
            sql.append(sqlCondition);
        }
        sqlCount.append(sql);
        sqlSelect.append(sql);
        sqlSelect.append(" ORDER BY ").append(pageable.getSort().toString().replace(":", ""));
        Query queryCount = em.createNativeQuery(sqlCount.toString());
        Query querySelect = em.createNativeQuery(sqlSelect.toString(), "search_applications");
        if (!ObjectUtils.isEmpty(portalIds)) {
            queryCount.setParameter("portalIds", portalIds);
            querySelect.setParameter("portalIds", portalIds);
        }
        if (Objects.nonNull(searchTerm.getCommunityPartnerId())) {
            queryCount.setParameter("communityPartnerId", searchTerm.getCommunityPartnerId());
            querySelect.setParameter("communityPartnerId", searchTerm.getCommunityPartnerId());
        }
        if (Objects.nonNull(searchTerm.getStatus())) {
            queryCount.setParameter("status", searchTerm.getStatus());
            querySelect.setParameter("status", searchTerm.getStatus());
        }
        if (Objects.nonNull(searchTerm.getExcludeStatus())) {
            queryCount.setParameter("excludeStatus", searchTerm.getExcludeStatus());
            querySelect.setParameter("excludeStatus", searchTerm.getExcludeStatus());
        }

        if (!ObjectUtils.isEmpty(searchTerm.getSearchKeyword())) {
            String wildcards = StringUtils.wildcards(searchTerm.getSearchKeyword());
            queryCount.setParameter("searchKeyword", wildcards);
            querySelect.setParameter("searchKeyword", wildcards);
        }
        long total = (Long) queryCount.getSingleResult();
        querySelect.setFirstResult((pageable.getPageNumber()) * pageable.getPageSize());
        querySelect.setMaxResults(pageable.getPageSize());
        var results = querySelect.getResultList();
        return new PageImpl<>(results, pageable, total);
    }
}
