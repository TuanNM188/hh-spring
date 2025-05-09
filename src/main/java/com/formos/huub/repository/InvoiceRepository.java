package com.formos.huub.repository;

import com.formos.huub.domain.entity.Invoice;
import com.formos.huub.domain.response.invoice.IResponseInvoiceExport;
import com.formos.huub.domain.response.report.IResponseOverviewApplicationReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * ***************************************************
 * * Description :
 * * File        : InvoiceRepository
 * * Author      : Hung Tran
 * * Date        : Mar 04, 2025
 * ***************************************************
 **/

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    @Query(
        value = """
               select * from (select
                        bou.id as userId,i.invoice_number as invoiceNumber,1 as HoursCompleted,
                        ptv.contracted_rate as contractedRate, ptv.contracted_rate as cost, bou.normalized_full_name as businessOwnerName,
                        TO_CHAR(a.appointment_date at TIME zone 'UTC' at TIME zone :timezone ,'MM/DD/YYYY HH:MI AM') as appointmentDate,
                        null as projectCompletionDate, c."name" as categoryName, so."name" as serviceName, tau.normalized_full_name as advisor,
                        apr.report_number as reportNumber
                    from appointment a\s
                    inner join invoice i  on a.invoice_id = i.id and a.status = 'INVOICED'
                    inner join technical_assistance_submit tas on tas.id = a.technical_assistance_submit_id
                    inner join program_term_vendor ptv on ptv.id = tas.assign_vendor_id
                    inner join appointment_detail ad on ad.appointment_id = a.id
                  left join appointment_report apr on apr.appointment_id = a.id
                  left join technical_advisor ta on ta.id = a.technical_advisor_id
                  left join jhi_user tau on tau.id = ta.user_id
                    left join jhi_user bou on bou.id = a.user_id
                    left join category c on c.id = ad.category_id
                    left join service_offered so on so.id = ad.service_id
                    where a.id is not null
               	    and (:portalId is null or tas.portal_id = :portalId)
                    and (:startDate is null
                        OR TO_CHAR(i.created_date AT TIME ZONE 'UTC' AT TIME ZONE :timezone, 'YYYY-MM-DD') between :startDate and :endDate)
               union all
               	select
                    bou.id as userId, i.invoice_number as invoiceNumber, pr.estimated_hours_needed as HoursCompleted,
                    ptv.contracted_rate as contractedRate, pr.estimated_hours_needed * ptv.contracted_rate as cost,
                    bou.normalized_full_name, null as appointmentDate,
                    TO_CHAR(pr.completed_date at TIME zone 'UTC' at TIME zone :timezone,'MM/DD/YYYY HH:MI AM' ) as projectCompletionDate,
                    c."name" as categoryName, so."name" as serviceName, tau.normalized_full_name as advisor, prr.report_number as reportNumber
                from project pr
                inner join invoice i on pr.invoice_id = i.id and pr.status = 'INVOICED'
                inner join technical_assistance_submit tas on tas.id = pr.technical_assistance_submit_id
                inner join program_term_vendor ptv on ptv.id = tas.assign_vendor_id
                left join project_report prr on prr.project_id = pr.id
                left join technical_advisor ta on ta.id = pr.technical_advisor_id
                left join jhi_user tau on tau.id = ta.user_id
                left join business_owner bo on pr.business_owner_id = bo.id
                left join jhi_user bou on bou.id = bo.user_id
                left join category c on c.id = pr.category_id
                left join service_offered so on so.id = pr.service_id
                where pr.id is not null
               	    and (:portalId is null or tas.portal_id = :portalId)
                    and (:startDate is null
                        OR TO_CHAR(i.created_date AT TIME ZONE 'UTC' AT TIME ZONE :timezone, 'YYYY-MM-DD') between :startDate and :endDate)
                 ) as i
               left join (
               	select
               		uaf.entry_id as userId,
               		uaf.additional_answer as businessName
               	from user_answer_form uaf
               	join question q on uaf.question_id = q.id
               	where uaf.entry_type = 'USER' and uaf.is_delete is false
               		and q.question_code = 'PORTAL_INTAKE_QUESTION_BUSINESS'
               	group by q.id, uaf.id
                 ) as bn on bn.userId = i.userId
        """,
        nativeQuery = true
    )
    List<IResponseInvoiceExport> getAllByPortalAndDate(UUID portalId, String startDate, String endDate, String timezone);

    /**
     * Find all invoices by technical advisor ID, portal ID, and invoice month.
     */
    List<Invoice> findByTechnicalAdvisorIdAndPortalIdAndInvoiceMonth(UUID technicalAdvisorId, UUID portalId, String invoiceMonth);


    @Query(value = "select coalesce(sum(id.amount), 0) as monthlyExpense, " +
        "   sum(coalesce(id.total_project_hour, 0 ) + coalesce(id.total_appointment_hour, 0)) as totalHours " +
        "        from invoice i inner join technical_advisor ta on ta.id = i.technical_advisor_id " +
        "        left join invoice_detail id on id.invoice_id = i.id" +
        "        where (coalesce(:portalIds) is null or i.portal_id IN :portalIds)" +
        " and (:startDate is null OR to_char(i.created_date at TIME zone 'UTC' at TIME zone :timezone, 'YYYY-MM-DD') " +
        " between :startDate and :endDate)", nativeQuery = true)
    IResponseOverviewApplicationReport getNumMonthlyExpense(List<UUID> portalIds, String startDate, String endDate, String timezone);
}
