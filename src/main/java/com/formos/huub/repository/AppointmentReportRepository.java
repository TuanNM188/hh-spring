package com.formos.huub.repository;

import com.formos.huub.domain.entity.AppointmentReport;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.formos.huub.domain.response.report.IResponseOverviewApplicationReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AppointmentReportRepository extends JpaRepository<AppointmentReport, UUID> {

    @Query(value = "SELECT  " +
        "    ROUND(COALESCE(SUM(r.rating)::DECIMAL / NULLIF(COUNT(CASE WHEN rating is not null and rating > 0 THEN 1 END), 0), 0), 2) AS advisorRatingAverage, " +
        "    count(distinct r.technicalAdvisorId) as numberOfAdvisor, sum(coalesce(r.useHours, 0)) as totalHours " +
        "FROM ( " +
        "    SELECT ad.rating::DECIMAL AS rating, p.id as portalId, a.technical_advisor_id as technicalAdvisorId, ad.use_award_hours as useHours, ar.created_date as createdDate " +
        "    FROM appointment a " +
        "    inner join technical_assistance_submit tas on tas.id = a.technical_assistance_submit_id " +
        "    inner join program_term pt on pt.id = tas.program_term_id and pt.is_current is true " +
        "    INNER JOIN appointment_detail ad ON a.id = ad.appointment_id " +
        "    INNER JOIN portal p ON p.id = a.portal_id " +
        "    INNER JOIN appointment_report ar on ar.appointment_id = a.id " +
        "    WHERE a.is_delete IS FALSE and (a.status = 'COMPLETED' or a.status = 'INVOICED')" +
        "      and (:portalIds is null or p.id IN (:portalIds)) " +
        "      and (:startDate is null OR TO_CHAR(ar.created_date at TIME zone 'UTC' at TIME zone :timezone , 'YYYY-MM-DD') between :startDate and :endDate)" +
        "    UNION ALL " +
        "    SELECT prj.rating::DECIMAL AS rating, p.id as portalId, prj.technical_advisor_id as technicalAdvisorId, pr.hours_completed as useHours, pr.created_date as createdDate " +
        "    FROM project prj " +
        "    inner join technical_assistance_submit tas on tas.id = prj.technical_assistance_submit_id " +
        "    inner join program_term pt on pt.id = tas.program_term_id and pt.is_current is true " +
        "    INNER JOIN portal p ON p.id = prj.portal_id " +
        "    INNER JOIN project_report pr on pr.project_id = prj.id " +
        "    WHERE prj.is_delete IS FALSE and (prj.status = 'COMPLETED' or prj.status = 'INVOICED') " +
        "      and (:portalIds is null or p.id IN (:portalIds)) " +
        "      and (:startDate is null OR TO_CHAR(pr.created_date at TIME zone 'UTC' at TIME zone :timezone , 'YYYY-MM-DD') between :startDate and :endDate)" +
        ") AS r ", nativeQuery = true)
    IResponseOverviewApplicationReport numAverageRatingAdvisor(List<UUID> portalIds, String startDate, String endDate, String timezone);

    @Query("select pr from AppointmentReport  pr join pr.appointment p where p.id = :appointmentId and p.isDelete = false")
    Optional<AppointmentReport> findByAppointmentId(UUID appointmentId);
}
