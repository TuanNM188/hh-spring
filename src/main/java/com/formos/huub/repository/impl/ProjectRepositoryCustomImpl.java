package com.formos.huub.repository.impl;

import com.formos.huub.domain.entity.Project;
import com.formos.huub.domain.request.project.RequestSearchProject;
import com.formos.huub.domain.request.project.RequestSearchProjectForBO;
import com.formos.huub.domain.request.tasurvey.RequestSearchTaSurveyProject;
import com.formos.huub.domain.response.project.ResponseProjectForBusinessOwner;
import com.formos.huub.domain.response.project.ResponseSearchProject;
import com.formos.huub.domain.response.tasurvey.ResponseTaSurveyProject;
import com.formos.huub.framework.utils.StringUtils;
import com.formos.huub.repository.ProjectRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.util.ObjectUtils;

import java.util.Objects;

public class ProjectRepositoryCustomImpl implements ProjectRepositoryCustom {

    private final EntityManager em;

    @Autowired
    public ProjectRepositoryCustomImpl(JpaContext context) {
        this.em = context.getEntityManagerByManagedType(Project.class);
    }

    @Override
    public Page<ResponseSearchProject> searchByTermAndCondition(RequestSearchProject condition, Pageable pageable) {
        StringBuilder sql = new StringBuilder();
        StringBuilder sqlCount = new StringBuilder("select count(DISTINCT pr.id) ");
        StringBuilder sqlSelect = new StringBuilder("SELECT " +
            "    pr.id as id,  " +
            "    pr.project_name as projectName,  " +
            "    pr.estimated_hours_needed as estimatedHoursNeeded, " +
            "    pr.completed_date as completedDate, " +
            "    bou.id as businessOwnerId, " +
            "    bou.normalized_full_name AS businessOwnerName, " +
            "    tau.id AS advisorId, " +
            "    tau.normalized_full_name AS advisorName, " +
            "    una.id as navigatorId, " +
            "    una.normalized_full_name as navigatorName, " +
            "    pr.status as status," +
            "    pr.created_date as requestDate," +
            "    pr.estimated_completion_date as estimatedCompletionDate ");
        sql.append(" " +
            "FROM Project pr " +
            "INNER JOIN business_owner bo on bo.id = pr.business_owner_id " +
            "INNER JOIN jhi_user bou on bo.user_id = bou.id " +
            "INNER JOIN portal p on p.id = pr.portal_id " +
            "INNER JOIN community_partner cp on cp.id = pr.vendor_id " +
            "INNER JOIN technical_advisor ta on ta.id = pr.technical_advisor_id " +
            "LEFT JOIN jhi_user tau on ta.user_id = tau.id and tau.is_delete is false " +
            "LEFT JOIN technical_assistance_submit tas on tas.id = pr.technical_assistance_submit_id and tas.is_delete is false " +
            "LEFT JOIN program_term pt on pt.id = tas.program_term_id " +
            "LEFT JOIN jhi_user una ON una.community_partner_id = cp.id AND una.is_navigator = TRUE and una.is_delete is false " +
            " WHERE 1 = 1 ");
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
            sql.append(" AND (lower(pr.project_name) like :searchKeyword OR LOWER(bou.normalized_full_name) like :searchKeyword " +
                "OR LOWER(tau.normalized_full_name) like :searchKeyword  " +
                "OR LOWER(una.normalized_full_name) like :searchKeyword)");
        }

        if (Objects.nonNull(condition.getProjectStatus())) {
            sql.append(" AND (pr.status IN :status)");
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
        Query querySelect = em.createNativeQuery(sqlSelect.toString(), "search_projects");
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
        if (Objects.nonNull(condition.getProjectStatus())) {
            queryCount.setParameter("status", condition.getProjectStatus());
            querySelect.setParameter("status", condition.getProjectStatus());
        }
        long total = (Long) queryCount.getSingleResult();
        querySelect.setFirstResult((pageable.getPageNumber()) * pageable.getPageSize());
        querySelect.setMaxResults(pageable.getPageSize());
        var results = querySelect.getResultList();
        return new PageImpl<>(results, pageable, total);
    }

    @Override
    public Page<ResponseTaSurveyProject> getRatingResponse(RequestSearchTaSurveyProject condition, Pageable pageable) {
        StringBuffer sql = new StringBuffer();
        StringBuilder sqlCount = new StringBuilder("select count(DISTINCT pr.id) ");
        StringBuilder sqlSelect = new StringBuilder("""
                SELECT
                    pr.id as projectId,pr.project_name as projectName,tau.id AS advisorId,tau.normalized_full_name AS advisorName,
                    bou.id as businessOwnerId,bou.normalized_full_name AS businessOwnerName,bou.email AS businessOwnerEmail,
                    cp.name as vendorName, pr.rating as rating, pr.feedback as feedback
            """);
        sql.append("""
                FROM project pr
                JOIN business_owner bo on bo.id = pr.business_owner_id
                JOIN jhi_user bou on bou.id = bo.user_id
                JOIN portal p on pr.portal_id = p.id
                JOIN community_partner cp on pr.vendor_id = cp.id
                JOIN technical_advisor ta on pr.technical_advisor_id = ta.id
                JOIN jhi_user tau on tau.id = ta.user_id
                WHERE pr.rating is not null and pr.rating > 0
            """);
        var portalIds = condition.getPortalIds();
        if (!ObjectUtils.isEmpty(portalIds)) {
            sql.append(" AND (p.id IN (:portalIds))");
        }
        if (Objects.nonNull(condition.getCommunityPartnerId())) {
            sql.append(" AND (cp.id = :communityPartnerId)");
        }
        if (!com.formos.huub.framework.utils.ObjectUtils.isEmpty(condition.getSearchKeyword())) {
            sql.append("""
                    AND (LOWER(pr.project_name) like :searchKeyword
                        OR LOWER(tau.normalized_full_name) like :searchKeyword
                        OR LOWER(bou.normalized_full_name) like :searchKeyword
                        OR LOWER(bou.email) like :searchKeyword
                        OR LOWER(cp.name) like :searchKeyword
                        OR LOWER(pr.feedback) like :searchKeyword
                    )
                """);
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
        Query querySelect = em.createNativeQuery(sqlSelect.toString(), "search_ta_survey_projects");
        if (!ObjectUtils.isEmpty(portalIds)) {
            queryCount.setParameter("portalIds", portalIds);
            querySelect.setParameter("portalIds", portalIds);
        }
        if (Objects.nonNull(condition.getCommunityPartnerId())) {
            queryCount.setParameter("communityPartnerId", condition.getCommunityPartnerId());
            querySelect.setParameter("communityPartnerId", condition.getCommunityPartnerId());
        }
        if (!com.formos.huub.framework.utils.ObjectUtils.isEmpty(condition.getSearchKeyword())) {
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

    @Override
    public Page<ResponseProjectForBusinessOwner> searchProjectForBusinessOwner(RequestSearchProjectForBO condition, Pageable pageable) {
        StringBuilder sqlCount = new StringBuilder("select count(DISTINCT pr.id) ");
        StringBuilder sqlSelect = new StringBuilder("""
            SELECT new com.formos.huub.domain.response.project.ResponseProjectForBusinessOwner(
                pr.id as projectId,
                tau.id AS technicalAdvisorId,
                pr.estimatedCompletionDate AS estimatedCompletionDate,
                pr.projectName as projectName,
                tau.normalizedFullName AS technicalAdvisorName,
                pr.status as status
            )""");
        StringBuffer sql = new StringBuffer("""
            FROM Project pr
            JOIN pr.businessOwner bo
            JOIN bo.user bou
            JOIN pr.technicalAdvisor ta
            JOIN ta.user tau
            JOIN pr.technicalAssistanceSubmit tas
            JOIN tas.programTerm pt
            WHERE pt.isCurrent is true and bou.id = :userId
            """);
        if (!ObjectUtils.isEmpty(condition.getSearchConditions())) {
            sql.append(" AND ");
            var sqlCondition = SqlQueryBuilder.generateSqlQuery(condition.getSearchConditions());
            sql.append(sqlCondition);
        }
        sqlCount.append(sql);
        sqlSelect.append(sql);
        sqlSelect.append(" ORDER BY ").append(pageable.getSort().toString().replace(":", ""));
        Query queryCount = em.createQuery(sqlCount.toString());
        Query querySelect = em.createQuery(sqlSelect.toString());
        queryCount.setParameter("userId", condition.getUserId());
        querySelect.setParameter("userId", condition.getUserId());
        long total = (Long) queryCount.getSingleResult();
        querySelect.setFirstResult((pageable.getPageNumber()) * pageable.getPageSize());
        querySelect.setMaxResults(pageable.getPageSize());
        var results = querySelect.getResultList();
        return new PageImpl<>(results, pageable, total);
    }
}
