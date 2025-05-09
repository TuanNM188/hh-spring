package com.formos.huub.repository.impl;

import com.formos.huub.domain.request.metricsreport.RequestSearchAppointmentProjectReport;
import com.formos.huub.domain.response.metricsreport.ResponseInvoicedAmountByAdvisor;
import com.formos.huub.domain.response.metricsreport.ResponseSearchAppointmentProjectReport;
import com.formos.huub.framework.utils.ObjectUtils;
import com.formos.huub.repository.MetricsReportRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Objects;

import static com.formos.huub.repository.impl.SqlQueryBuilder.generateSqlQuery;

@Repository
public class MetricsReportRepositoryCustomImpl implements MetricsReportRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<ResponseInvoicedAmountByAdvisor> getAllInvoiceAmountByAdvisorPageable(RequestSearchAppointmentProjectReport request, Pageable pageable) {
        StringBuilder sql = new StringBuilder();
        StringBuilder sqlCount = new StringBuilder("SELECT count(DISTINCT i.id) ");

        StringBuilder sqlSelect = new StringBuilder(
            "select * "
        );
        sql.append(" from ( select i.created_date as invoiceDate, i.id as id, tau.id as advisorId, tau.normalized_full_name as advisorName, i.portal_id as portalId," +
            " sum(case when id.is_appointment is true then id.total_appointment_hour else 0 end) as appointmentHours," +
            " sum(case when id.is_appointment is null or id.is_appointment is false then id.total_appointment_hour else 0 end) as projectHours, " +
            " sum(id.amount) as totalAmount, i.file_path as filePath, i.file_name as fileName from invoice i left join invoice_detail id on i.id = id.invoice_id " +
            "left join technical_advisor ta on ta.id = i.technical_advisor_id " +
            "left join jhi_user tau on tau.id = ta.user_id  ");
        sql.append(" WHERE i.is_delete is false group by i.id, tau.id ) as i where 1 = 1 ");
        var portalIds = request.getPortalIds();
        if (!ObjectUtils.isEmpty(portalIds)) {
            sql.append(" AND (i.portalId IN (:portalIds))");
        }

        if (Objects.nonNull(request.getStartDate()) && Objects.nonNull(request.getEndDate())) {
            sql.append(" AND (TO_CHAR(invoiceDate at TIME zone 'UTC' at TIME zone :timezone , 'YYYY-MM-DD') between :startDate and :endDate)");
        }
        if (!ObjectUtils.isEmpty(request.getSearchConditions())) {
            sql.append(" AND ");
            var sqlCondition = generateSqlQuery(request.getSearchConditions());
            sql.append(sqlCondition);
        }
        sqlCount.append(sql);
        sqlSelect.append(sql);
        sqlSelect.append(" ORDER BY ").append(pageable.getSort().toString().replace(":", ""));
        Query queryCount = em.createNativeQuery(sqlCount.toString());
        Query querySelect = em.createNativeQuery(sqlSelect.toString(), "search_invoice_amount_by_advisor");
        if (!ObjectUtils.isEmpty(portalIds)) {
            queryCount.setParameter("portalIds", portalIds);
            querySelect.setParameter("portalIds", portalIds);
        }
        if (Objects.nonNull(request.getStartDate()) && Objects.nonNull(request.getEndDate())) {
            queryCount.setParameter("startDate", request.getStartDate());
            querySelect.setParameter("startDate", request.getStartDate());
            queryCount.setParameter("endDate", request.getEndDate());
            querySelect.setParameter("endDate", request.getEndDate());
            queryCount.setParameter("timezone", request.getTimezone());
            querySelect.setParameter("timezone", request.getTimezone());
        }
        querySelect.setFirstResult((pageable.getPageNumber()) * pageable.getPageSize());
        querySelect.setMaxResults(pageable.getPageSize());

        var results = querySelect.getResultList();
        long total = (Long) queryCount.getSingleResult();
        return new PageImpl<>(results, pageable, total);
    }

    @Override
    public Page<ResponseSearchAppointmentProjectReport> searchAppointmentProjectReports(RequestSearchAppointmentProjectReport request, Pageable pageable) {
        StringBuilder sql = new StringBuilder();
        StringBuilder sqlCount = new StringBuilder("SELECT COUNT(id) ");
        StringBuilder sqlSelect = new StringBuilder("SELECT * ");
        sql.append(" from (SELECT rp.id AS id, rp.created_date AS reportSubmissionDate, i.invoice_number  AS invoiceNumber," +
            " bou.normalized_full_name AS businessOwnerName," +
            "            tau.normalized_full_name AS advisorName, (ad.use_award_hours)::FLOAT AS hours , rp.pdf_url AS pdfUrl, rp.pdf_filename as pdfFilename, p.id as portalId " +
            "           FROM appointment_report rp" +
            "            JOIN appointment a ON rp.appointment_id = a.id  and a.is_delete is false" +
            "            JOIN portal p ON a.portal_id = p.id and p.status = 'ACTIVE' " +
            "            JOIN appointment_detail ad ON a.id = ad.appointment_id" +
            "            JOIN jhi_user bou ON a.user_id = bou.id and bou.is_delete is false " +
            "            JOIN technical_advisor ta ON a.technical_advisor_id = ta.id" +
            "            JOIN jhi_user tau ON ta.user_id = tau.id" +
            "            LEFT JOIN invoice i on i.id = a.invoice_id  " +
            " UNION ALL" +
            "            SELECT" +
            "            rp.id AS id, rp.created_date AS reportSubmissionDate, i.invoice_number  AS invoiceNumber, bou.normalized_full_name AS businessOwnerName," +
            "            tau.normalized_full_name AS advisorName, (rp.hours_completed)::FLOAT AS hours , rp.pdf_url AS pdfUrl, rp.pdf_filename as pdfFilename, p.id as portalId" +
            "            FROM project_report rp" +
            "            JOIN project prj ON rp.project_id = prj.id and prj.is_delete is false " +
            "            JOIN business_owner bo ON prj.business_owner_id = bo.id" +
            "            JOIN jhi_user bou ON bo.user_id = bou.id and bou.is_delete is false " +
            "            JOIN technical_advisor ta ON prj.technical_advisor_id = ta.id" +
            "            JOIN jhi_user tau ON ta.user_id = tau.id  and tau.is_delete is false " +
            "            LEFT JOIN invoice i on i.id = prj.invoice_id " +
            "            JOIN portal p ON prj.portal_id = p.id and p.status = 'ACTIVE' ) as combined_results where 1 = 1 ");

        var portalIds = request.getPortalIds();
        if (!ObjectUtils.isEmpty(portalIds)) {
            sql.append(" AND (portalId In (:portalIds)) ");
        }
        if (Objects.nonNull(request.getStartDate()) && Objects.nonNull(request.getEndDate())) {
            sql.append(" AND (TO_CHAR(reportSubmissionDate at TIME zone 'UTC' at TIME zone :timezone , 'YYYY-MM-DD') between :startDate and :endDate)");
        }
        if (!ObjectUtils.isEmpty(request.getSearchConditions())) {
            sql.append(" AND ");
            var sqlCondition = generateSqlQuery(request.getSearchConditions());
            sql.append(sqlCondition);
        }
        sqlCount.append(sql);
        sqlSelect.append(sql);
        sqlSelect.append(" ORDER BY ").append(pageable.getSort().toString().replace(":", ""));

        Query queryCount = em.createNativeQuery(sqlCount.toString());
        Query querySelect = em.createNativeQuery(sqlSelect.toString(), "search_appointment_project_report");
        if (!ObjectUtils.isEmpty(portalIds)) {
            queryCount.setParameter("portalIds", portalIds);
            querySelect.setParameter("portalIds", portalIds);
        }
        if (Objects.nonNull(request.getStartDate()) && Objects.nonNull(request.getEndDate())) {
            queryCount.setParameter("startDate", request.getStartDate());
            querySelect.setParameter("startDate", request.getStartDate());
            queryCount.setParameter("endDate", request.getEndDate());
            querySelect.setParameter("endDate", request.getEndDate());
            queryCount.setParameter("timezone", request.getTimezone());
            querySelect.setParameter("timezone", request.getTimezone());
        }
        querySelect.setFirstResult((pageable.getPageNumber()) * pageable.getPageSize());
        querySelect.setMaxResults(pageable.getPageSize());
        var results = querySelect.getResultList();
        long total = (Long) queryCount.getSingleResult();

        return new PageImpl<>(results, pageable, total);
    }

}
