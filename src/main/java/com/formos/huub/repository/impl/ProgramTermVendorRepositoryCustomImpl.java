package com.formos.huub.repository.impl;

import com.formos.huub.domain.entity.ProgramTermVendor;
import com.formos.huub.domain.request.vendor.RequestSearchVendor;
import com.formos.huub.domain.response.vendor.ResponseSearchVendors;
import com.formos.huub.repository.ProgramTermVendorRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.util.ObjectUtils;

import java.util.Objects;

public class ProgramTermVendorRepositoryCustomImpl implements ProgramTermVendorRepositoryCustom {
    private final EntityManager em;

    @Autowired
    public ProgramTermVendorRepositoryCustomImpl(JpaContext context) {
        this.em = context.getEntityManagerByManagedType(ProgramTermVendor.class);
    }
    @Override
    public Page<ResponseSearchVendors>  searchByTermAndCondition(RequestSearchVendor searchTerm, Pageable pageable) {
        StringBuilder sql = new StringBuilder();
        StringBuilder sqlCount = new StringBuilder("select count(distinct vendor.id) ");
        StringBuilder sqlSelect = new StringBuilder("SELECT *");
        sql.append(" FROM " +
            "  (SELECT ptv.id AS id, " +
            "          cp.id AS communityPartnerId, " +
            "          cp.name communityPartnerName, " +
            "          nau.id AS navigatorId, " +
            "          pro.portal_id AS portalId, " +
            "          nau.normalized_full_name AS navigatorName, " +
            "          COALESCE(assign.totalAssigns, 0) AS totalAssigns, " +
            "          ptv.created_date AS createdDate, " +
            "          COALESCE(ROUND((100.0 * COALESCE(ptv.vendor_budget, 0) / NULLIF(pt.budget, 0))::NUMERIC, 2)::FLOAT, 0) AS budgetAssign, " +
            "          COALESCE(ROUND((100.0 * ((COALESCE(ap.hoursInProgress, 0) + COALESCE(prj.hoursInProgress, 0)) * ptv.contracted_rate) / NULLIF(pt.budget, 0))::NUMERIC, 2)::FLOAT, 0) AS budgetInProgress, " +
            "          COALESCE(ROUND((100.0 * ((COALESCE(ap.hoursCompleted, 0) + COALESCE(prj.hoursCompleted, 0)) * ptv.contracted_rate) / NULLIF(pt.budget, 0))::NUMERIC, 2)::FLOAT, 0) AS budgetCompleted " +
            "   FROM program_term_vendor ptv " +
            "   INNER JOIN program_term pt ON pt.id = ptv.program_term_id " +
            "   AND ptv.is_delete IS FALSE " +
            "   AND pt.is_current IS TRUE " +
            "   INNER JOIN community_partner cp ON cp.id = ptv.vendor_id " +
            "   INNER JOIN program pro ON pro.id = pt.program_id " +
            "   AND pt.is_delete IS FALSE " +
            "   AND pt.is_current IS TRUE " +
            "   LEFT JOIN jhi_user nau ON nau.community_partner_id = cp.id " +
            "   AND nau.is_navigator IS TRUE and nau.is_delete is false" +
            "   LEFT JOIN  (select ptv2.id as program_term_vendor_id, count(tas.id) as totalAssigns " +
            "               from program_term_vendor ptv2 left join technical_assistance_submit tas " +
            "               ON tas.assign_vendor_id = ptv2.id AND tas.status NOT IN ('SUBMITTED', 'DENIED') group by ptv2.id" +
            "               ) as assign on assign.program_term_vendor_id = ptv.id" +
            "   LEFT JOIN " +
            "     (SELECT tas.assign_vendor_id AS assign_vendor_id, " +
            "             sum(CASE " +
            "                     WHEN a.status IN ('SCHEDULED', 'REPORT_REQUIRED', 'OVERDUE') THEN 1 " +
            "                     ELSE 0 " +
            "                 END) AS hoursInProgress, " +
            "             sum(CASE " +
            "                     WHEN a.status IN ('INVOICED', 'COMPLETED') THEN 1 " +
            "                     ELSE 0 " +
            "                 END) AS hoursCompleted " +
            "      FROM appointment a " +
            "      INNER JOIN technical_assistance_submit tas ON a.technical_assistance_submit_id = tas.id " +
            "      AND tas.status NOT IN ('SUBMITTED', " +
            "                             'DENIED') " +
            "      INNER JOIN program_term pt ON tas.program_term_id = pt.id " +
            "      AND pt.is_current IS TRUE " +
            "      GROUP BY tas.assign_vendor_id) AS ap ON ptv.id = ap.assign_vendor_id " +
            "   LEFT JOIN " +
            "     (SELECT tas.assign_vendor_id AS assign_vendor_id, " +
            "             sum(CASE " +
            "                     WHEN p.status IN ('PROPOSED', 'VENDOR_APPROVED', 'WORK_IN_PROGRESS', 'OVERDUE') THEN p.estimated_hours_needed " +
            "                     ELSE 0 " +
            "                 END) AS hoursInProgress, " +
            "             sum(CASE " +
            "                     WHEN p.status IN ('INVOICED', 'COMPLETED') THEN p.estimated_hours_needed " +
            "                     ELSE 0 " +
            "                 END) AS hoursCompleted " +
            "      FROM project p " +
            "      INNER JOIN technical_assistance_submit tas ON p.technical_assistance_submit_id = tas.id " +
            "      AND tas.status NOT IN ('SUBMITTED', " +
            "                             'DENIED') " +
            "      INNER JOIN program_term pt ON tas.program_term_id = pt.id " +
            "      AND pt.is_current IS TRUE " +
            "      GROUP BY tas.assign_vendor_id) AS prj ON ptv.id = prj.assign_vendor_id) AS vendor " +
            "WHERE 1 = 1");
        var portalIds = searchTerm.getPortalIds();
        if (!ObjectUtils.isEmpty(portalIds)) {
            sql.append(" AND vendor.portalId IN (:portalIds)");
        }
        if (Objects.nonNull(searchTerm.getCommunityPartnerId())) {
            sql.append(" AND vendor.communityPartnerId = :communityPartnerId");
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
        Query querySelect = em.createNativeQuery(sqlSelect.toString(), "search_vendors");
        if (!ObjectUtils.isEmpty(portalIds)) {
            queryCount.setParameter("portalIds", portalIds);
            querySelect.setParameter("portalIds", portalIds);
        }
        if (Objects.nonNull(searchTerm.getCommunityPartnerId())) {
            queryCount.setParameter("communityPartnerId", searchTerm.getCommunityPartnerId());
            querySelect.setParameter("communityPartnerId", searchTerm.getCommunityPartnerId());
        }

        if (Objects.nonNull(searchTerm.getSearchKeyword())) {
            queryCount.setParameter("searchKeyword", searchTerm.getSearchKeyword());
            querySelect.setParameter("searchKeyword", searchTerm.getSearchKeyword());
        }
        long total = (Long) queryCount.getSingleResult();
        querySelect.setFirstResult((pageable.getPageNumber()) * pageable.getPageSize());
        querySelect.setMaxResults(pageable.getPageSize());
        var results = querySelect.getResultList();
        return new PageImpl<>(results, pageable, total);
    }
}
