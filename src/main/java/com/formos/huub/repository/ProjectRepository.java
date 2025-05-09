/**
 * ***************************************************
 * * Description :
 * * File        : ProjectRepository
 * * Author      : Hung Tran
 * * Date        : Jan 20, 2025
 * ***************************************************
 **/
package com.formos.huub.repository;

import com.formos.huub.domain.entity.*;
import com.formos.huub.domain.enums.ProjectStatusEnum;
import com.formos.huub.domain.response.project.IResponseCountProject;
import com.formos.huub.domain.response.project.IResponseProjectExport;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID>, ProjectRepositoryCustom {
    @Query(
        value = """
                    select
                       	p.platform_name as portalName, TO_CHAR(pr.created_date at TIME zone 'UTC' at TIME zone :timezone ,'MM/DD/YYYY') as createdDate,
                       	u.first_name as firstName, u.last_name as lastName, u.email as email,
                       	bn.businessName as businessName, tau.normalized_full_name as advisorName, cp."name" as vendor, pr.project_name  as projectName,
                       	pr.scope_of_work as scopeOfWork,  pr.status as status, pr.estimated_hours_needed as estimatedHoursNeeded,
                       	TO_CHAR(pr.proposed_start_date at TIME zone 'UTC' at TIME zone :timezone ,'MM/DD/YYYY') as proposedStartDate,
                       	TO_CHAR(pr.estimated_completion_date at TIME zone 'UTC' at TIME zone :timezone ,'MM/DD/YYYY') as estimatedCompletionDate,
                       	TO_CHAR(a.relatedAppointmentDate at TIME zone 'UTC' at TIME zone :timezone ,'MM/DD/YYYY HH:MI AM') as relatedAppointmentDate,
                       	c.name as categoryName, so.name as serviceName, i.invoice_number as invoiceNumber, prr.report_number as reportNumber
                       from
                       	project pr
                       	inner join business_owner bo on pr.business_owner_id = bo.id
                       inner join jhi_user u on bo.user_id = u.id
                       inner join technical_assistance_submit tas on tas.id = pr.technical_assistance_submit_id
                       inner join program_term pt on pt.id = tas.program_term_id and pt.is_current is true
                       inner join portal p on pr.portal_id = p.id
                       inner join technical_advisor ta on ta.id = pr.technical_advisor_id
                       inner join jhi_user tau on tau.id = ta.user_id
                       inner join community_partner cp on cp.id = pr.vendor_id
                       left join project_report prr on prr.project_id = pr.id
                       left join category c on c.id = pr.category_id
                       left join service_offered so on so.id = pr.service_id
                       left join invoice i on pr.invoice_id = i.id
                       left join (
                       	select
                       		uaf.entry_id as userId,
                       		uaf.additional_answer as businessName
                       	from
                       		user_answer_form uaf join question q on uaf.question_id = q.id
                       	where uaf.entry_type = 'USER'
                       		and uaf.is_delete is false
                       		and q.question_code = 'PORTAL_INTAKE_QUESTION_BUSINESS'
                       	group by q.id, uaf.id) as bn on bn.userId = u.id
                       left join (SELECT tas2.id as technicalAssistanceSubmitId, a.technical_advisor_id, a.appointment_date as relatedAppointmentDate
                       		from appointment a
                                Join technical_assistance_submit tas2 on a.technical_assistance_submit_id = tas2.id and a.is_delete is false
                                inner join program_term pt2 on pt2.id = tas2.program_term_id and pt2.is_current is true
                                WHERE a.appointment_date > NOW() order by a.appointment_date asc limit 1) as a
                                on a.technicalAssistanceSubmitId = tas.id and pr.technical_advisor_id= a.technical_advisor_id
                  where (:portalId is null or p.id = :portalId)
                  and (:startDate is null
                  OR TO_CHAR(pr.created_date AT TIME ZONE 'UTC' AT TIME ZONE :timezone, 'YYYY-MM-DD') between :startDate and :endDate)
                  order by pr.created_date desc
            """,
        nativeQuery = true
    )
    List<IResponseProjectExport> getAllByPortalAndDate(UUID portalId, String startDate, String endDate, String timezone);

    Optional<Project> findByIdAndBusinessOwnerId(UUID projectId, UUID userId);

    boolean existsByProjectName(String projectName);

    @Query(
        "select count(1) > 0 as isExist from Project p join p.technicalAssistanceSubmit tas join p.technicalAdvisor ta " +
        " where p.status != '' and tas.id = :technicalAssistanceSubmitId" +
        " and (:technicalAdvisorId is null Or ta.id = :technicalAdvisorId)"
    )
    boolean existsByTechnicalAssistanceSubmitId(UUID technicalAssistanceSubmitId, UUID technicalAdvisorId);

    @Transactional
    @Modifying
    @Query("UPDATE Project p SET p.status = :statusUpdated WHERE p.id in (:projectIds)")
    int updateProjectStatusByIds(List<UUID> projectIds, ProjectStatusEnum statusUpdated);

    @Query("SELECT p.id FROM  Project p WHERE p.status in :listStatus AND p.proposedStartDate < :end")
    List<UUID> findProjectIdByStatusAndBeforeProposedStartDate(List<ProjectStatusEnum> listStatus, Instant end);

    @Query("SELECT p FROM  Project p WHERE p.status = :status AND p.createdDate >= :threeDaysAgoStart AND p.createdDate < :threeDaysAgoEnd")
    List<Project> findProjectIdByStatus(ProjectStatusEnum status, Instant threeDaysAgoStart, Instant threeDaysAgoEnd);

    @Query(
        value = """
        select
        	count( CASE WHEN prj.status = 'PROPOSED' THEN 1 END) as numProposed,
        	count( CASE WHEN prj.status = 'VENDOR_APPROVED' THEN 1  END) as numVendorApproved,
        	count( CASE WHEN prj.status = 'WORK_IN_PROGRESS' THEN 1 END) as numWorkInProgress,
        	count( CASE WHEN prj.status = 'COMPLETED' THEN 1  END) as numCompleted,
        	count( CASE WHEN prj.status = 'OVERDUE' THEN 1  END) as numOverdue,
        	count( CASE WHEN prj.status = 'INVOICED' THEN 1  END) as numInvoiced,
        	count( CASE WHEN prj.status = 'DENIED' THEN 1  END) as numDenied
        from
        	portal p
        	inner join "program" pr on pr.portal_id = p.id and p.status = 'ACTIVE'
        	inner join program_term pt on pr.id = pt.program_id and pt.is_current is true and pt.is_delete is false
            left join project prj on prj.portal_id = p.id and prj.is_delete is false
        	join technical_assistance_submit tas ON prj.technical_assistance_submit_id = tas.id
        	 	 and tas.is_delete is false and tas.status = 'APPROVED' and pt.id = tas.program_term_id
            where (:portalIds is null OR p.id IN :portalIds)
            and (:communityPartnerId is null OR prj.vendor_id = :communityPartnerId)
            and (:technicalAdvisorId is null OR prj.technical_advisor_id = :technicalAdvisorId)
            """,
        nativeQuery = true
    )
    IResponseCountProject countByPortalIdAndStatus(List<UUID> portalIds, UUID communityPartnerId, UUID technicalAdvisorId);

    @Query(
        """
            SELECT ROUND(AVG(p.rating), 2)
            FROM Project p
            WHERE (:portalId is null or p.portal.id = :portalId)
                AND p.rating is not null and p.rating > 0
        """
    )
    Optional<Float> getAvgRatingByPortal(UUID portalId);

    @Query(
        """
            SELECT ROUND(AVG(p.rating), 2)
            FROM Project p
            WHERE p.vendor.id = :communityPartnerId
                AND p.rating is not null and p.rating > 0
        """
    )
    Optional<Float> getAvgRatingByCommunityPartnerId(UUID communityPartnerId);

    @Query(
        "SELECT p FROM Project p WHERE p.estimatedCompletionDate >= :startDate AND p.estimatedCompletionDate < :endDate AND p.status = 'WORK_IN_PROGRESS'"
    )
    List<Project> findByEstimatedCompletionDateBetween(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    @Query("select pr from Project  pr where pr.appointmentId = :appointmentId and pr.isDelete = false")
    Optional<Project> findByAppointmentId(UUID appointmentId);

    /**
     * Find projects by technical advisor, portal, status and completed date range
     */
    List<Project> findByTechnicalAdvisorAndPortalAndStatusAndCompletedDateBetween(
        TechnicalAdvisor technicalAdvisor,
        Portal portal,
        ProjectStatusEnum status,
        Instant startDate,
        Instant endDate
    );

    /**
     * Find projects by status and completed date range
     */
    List<Project> findByStatusAndCompletedDateBetween(ProjectStatusEnum status, Instant startDate, Instant endDate);

    @Transactional
    @Modifying
    @Query(
        "UPDATE Project p set p.status = :statusUpdated WHERE p.status = :status AND :currentDate > p.estimatedCompletionDate AND NOT EXISTS ( SELECT 1 FROM ProjectReport pr WHERE pr.project = p)"
    )
    int updateStatusProjectByStatusAndNoReportSubmitted(ProjectStatusEnum statusUpdated, ProjectStatusEnum status, Instant currentDate);

    boolean existsById(UUID projectId);

    boolean existsByIdAndBusinessOwner(UUID projectId, BusinessOwner businessOwner);

    @Query("select count(1) > 0 as isExist from Project p join p.technicalAdvisor ta " +
        " join p.technicalAssistanceSubmit tas " +
        " join tas.programTerm pt " +
        " where pt.isCurrent is true " +
        " and p.status != 'DENIED' and p.status != 'INVOICED' " +
        " and ta.id = :technicalAdvisorId")
    boolean existsUseByTechnicalAdvisor(UUID technicalAdvisorId);
}
