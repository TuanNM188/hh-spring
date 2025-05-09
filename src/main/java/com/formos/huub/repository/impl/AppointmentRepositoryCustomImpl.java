package com.formos.huub.repository.impl;

import com.formos.huub.domain.entity.Appointment;
import com.formos.huub.domain.request.appointmentmanagement.RequestSearchAppointment;
import com.formos.huub.domain.request.tasurvey.RequestSearchTaSurveyAppointment;
import com.formos.huub.domain.response.appointment.ResponseSearchAppointment;
import com.formos.huub.domain.response.tasurvey.ResponseTaSurveyAppointment;
import com.formos.huub.framework.utils.ObjectUtils;
import com.formos.huub.framework.utils.StringUtils;
import com.formos.huub.repository.AppointmentRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaContext;

public class AppointmentRepositoryCustomImpl implements AppointmentRepositoryCustom {

    private final EntityManager em;

    @Autowired
    public AppointmentRepositoryCustomImpl(JpaContext context) {
        this.em = context.getEntityManagerByManagedType(Appointment.class);
    }

    @Override
    public Page<ResponseSearchAppointment> searchByTermAndCondition(RequestSearchAppointment condition, Pageable pageable) {
        StringBuilder sql = new StringBuilder();
        StringBuilder sqlCount = new StringBuilder("select count(DISTINCT a.id) ");
        StringBuilder sqlSelect = new StringBuilder(
            "SELECT a.id AS id, " +
            " a.appointment_date AS appointmentDate, a.timezone AS timezone, u.id AS businessOwnerId," +
            " u.normalized_full_name AS businessOwnerName," +
            " tau.id AS advisorId, tau.normalized_full_name AS advisorName, una.id as navigatorId, una.normalized_full_name as navigatorName," +
            " cp.name as vendorName, c.name as categoryName, so.name as serviceName, a.status as status "
        );
        sql.append(
            " " +
            "FROM Appointment a " +
            "INNER JOIN appointment_detail ad on ad.appointment_id = a.id and a.is_delete is false " +
            "INNER JOIN portal p on p.id = a.portal_id " +
            "INNER JOIN community_partner cp on cp.id = a.community_partner_id " +
            "INNER JOIN technical_advisor ta on ta.id = a.technical_advisor_id " +
            "INNER JOIN jhi_user tau on tau.id = ta.user_id and tau.is_delete is false " +
            "INNER JOIN jhi_user u on u.id = a.user_id and u.is_delete is false " +
            "INNER JOIN technical_assistance_submit tas on tas.id = a.technical_assistance_submit_id and tas.is_delete is false " +
            "INNER JOIN program_term pt on pt.id = tas.program_term_id and pt.is_delete is false " +
            "LEFT JOIN jhi_user una ON una.community_partner_id = cp.id AND una.is_navigator = TRUE and una.is_delete is false " +
            "LEFT JOIN category c ON c.id = ad.category_id and c.is_delete is false " +
            "LEFT JOIN service_offered so ON so.id = ad.service_id and ad.is_delete is false " +
            "WHERE 1 = 1 "
        );

        if (Objects.nonNull(condition.getIsCurrent())) {
            sql.append(" AND (pt.is_current = :isCurrent)");
        }
        var portalIds = condition.getPortalIds();
        if (!ObjectUtils.isEmpty(portalIds)) {
            sql.append(" AND (p.id IN (:portalIds))");
        }
        if (Objects.nonNull(condition.getTechnicalAssistanceId())) {
            sql.append(" AND (tas.id = :technicalAssistanceId)");
        }
        if (Objects.nonNull(condition.getCommunityPartnerId())) {
            sql.append(" AND (cp.id = :communityPartnerId)");
        }
        if (Objects.nonNull(condition.getTechnicalAdvisorId())) {
            sql.append(" AND (ta.id = :technicalAdvisorId)");
        }
        if (!ObjectUtils.isEmpty(condition.getSearchKeyword())) {
            sql.append(
                " AND (LOWER(u.normalized_full_name) like :searchKeyword  OR LOWER(tau.normalized_full_name) like :searchKeyword  " +
                "OR LOWER(una.normalized_full_name) like :searchKeyword OR lower(c.name) like :searchKeyword OR lower(cp.name) like :searchKeyword)"
            );
        }
        if (Objects.nonNull(condition.getStartDate()) && Objects.nonNull(condition.getEndDate())) {
            sql.append(
                " AND (TO_CHAR(a.appointment_date at TIME zone 'UTC' at TIME zone :timezone , 'YYYY-MM-DD') between :startDate and :endDate)"
            );
        }
        if (Objects.nonNull(condition.getAppointmentStatus())) {
            sql.append(" AND (a.status IN :status)");
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
        Query querySelect = em.createNativeQuery(sqlSelect.toString(), "search_appointments");
        if (Objects.nonNull(condition.getIsCurrent())) {
            queryCount.setParameter("isCurrent", condition.getIsCurrent());
            querySelect.setParameter("isCurrent", condition.getIsCurrent());
        }
        if (!ObjectUtils.isEmpty(portalIds)) {
            queryCount.setParameter("portalIds", portalIds);
            querySelect.setParameter("portalIds", portalIds);
        }
        if (Objects.nonNull(condition.getCommunityPartnerId())) {
            queryCount.setParameter("communityPartnerId", condition.getCommunityPartnerId());
            querySelect.setParameter("communityPartnerId", condition.getCommunityPartnerId());
        }

        if (Objects.nonNull(condition.getTechnicalAdvisorId())) {
            queryCount.setParameter("technicalAdvisorId", condition.getTechnicalAdvisorId());
            querySelect.setParameter("technicalAdvisorId", condition.getTechnicalAdvisorId());
        }

        if (Objects.nonNull(condition.getTechnicalAssistanceId())) {
            queryCount.setParameter("technicalAssistanceId", condition.getTechnicalAssistanceId());
            querySelect.setParameter("technicalAssistanceId", condition.getTechnicalAssistanceId());
        }
        if (!ObjectUtils.isEmpty(condition.getSearchKeyword())) {
            var searchKeyword = StringUtils.makeStringWithContain(condition.getSearchKeyword());
            queryCount.setParameter("searchKeyword", searchKeyword);
            querySelect.setParameter("searchKeyword", searchKeyword);
        }
        if (Objects.nonNull(condition.getAppointmentStatus())) {
            queryCount.setParameter("status", condition.getAppointmentStatus());
            querySelect.setParameter("status", condition.getAppointmentStatus());
        }
        if (Objects.nonNull(condition.getStartDate()) && Objects.nonNull(condition.getEndDate())) {
            queryCount.setParameter("startDate", condition.getStartDate());
            querySelect.setParameter("startDate", condition.getStartDate());
            queryCount.setParameter("endDate", condition.getEndDate());
            querySelect.setParameter("endDate", condition.getEndDate());
            queryCount.setParameter("timezone", condition.getTimezone());
            querySelect.setParameter("timezone", condition.getTimezone());
        }
        long total = (Long) queryCount.getSingleResult();
        querySelect.setFirstResult((pageable.getPageNumber()) * pageable.getPageSize());
        querySelect.setMaxResults(pageable.getPageSize());
        var results = querySelect.getResultList();
        return new PageImpl<>(results, pageable, total);
    }

    @Override
    public Page<ResponseTaSurveyAppointment> getRatingResponse(RequestSearchTaSurveyAppointment condition, Pageable pageable) {
        StringBuffer sql = new StringBuffer();
        StringBuilder sqlCount = new StringBuilder("select count(DISTINCT a.id) ");
        StringBuilder sqlSelect = new StringBuilder("""
                SELECT DISTINCT
                    a.id AS appointmentId, a.appointment_date AS appointmentDate, a.timezone AS timezone, u.id AS businessOwnerId,
                    u.normalized_full_name AS businessOwnerName, tau.id AS advisorId, tau.normalized_full_name AS advisorName,
                    u.email as businessOwnerEmail, cp.name as vendorName, ad.rating as rating, ad.feedback as feedback
            """);
        sql.append("""
                FROM appointment a
                JOIN appointment_detail ad ON ad.appointment_id = a.id
                JOIN portal p ON p.id = a.portal_id
                JOIN community_partner cp ON cp.id = a.community_partner_id
                JOIN technical_advisor ta ON ta.id = a.technical_advisor_id
                JOIN jhi_user tau ON tau.id = ta.user_id
                JOIN jhi_user u ON u.id = a.user_id
                WHERE ad.rating is not null and ad.rating > 0
            """
        );
        var portalIds = condition.getPortalIds();
        if (!ObjectUtils.isEmpty(portalIds)) {
            sql.append(" AND (p.id IN (:portalIds))");
        }
        if (Objects.nonNull(condition.getCommunityPartnerId())) {
            sql.append(" AND (cp.id = :communityPartnerId)");
        }
        if (!ObjectUtils.isEmpty(condition.getSearchKeyword())) {
            sql.append("""
                    AND (LOWER(u.normalized_full_name) like :searchKeyword
                        OR LOWER(tau.normalized_full_name) like :searchKeyword
                        OR LOWER(cp.name) like :searchKeyword
                        OR LOWER(ad.feedback) like :searchKeyword
                    )
                """
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
        Query querySelect = em.createNativeQuery(sqlSelect.toString(), "search_ta_survey_appointments");
        if (!ObjectUtils.isEmpty(portalIds)) {
            queryCount.setParameter("portalIds", portalIds);
            querySelect.setParameter("portalIds", portalIds);
        }
        if (Objects.nonNull(condition.getCommunityPartnerId())) {
            queryCount.setParameter("communityPartnerId", condition.getCommunityPartnerId());
            querySelect.setParameter("communityPartnerId", condition.getCommunityPartnerId());
        }
        if (!ObjectUtils.isEmpty(condition.getSearchKeyword())) {
            var searchKeyword = StringUtils.makeStringWithContain(condition.getSearchKeyword());
            queryCount.setParameter("searchKeyword", searchKeyword);
            querySelect.setParameter("searchKeyword", searchKeyword);
        }
        long total = (Long) queryCount.getSingleResult();
        querySelect.setFirstResult((pageable.getPageNumber()) * pageable.getPageSize());
        querySelect.setMaxResults(pageable.getPageSize());
        var results = querySelect.getResultList();
        return new PageImpl<>(results, pageable, total);
    }
}
