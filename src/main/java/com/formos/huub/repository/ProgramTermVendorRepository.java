package com.formos.huub.repository;

import com.formos.huub.domain.entity.ProgramTermVendor;
import com.formos.huub.domain.response.technicalassistance.IResponseTermVendor;
import com.formos.huub.domain.response.vendor.IResponseCountVendor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProgramTermVendorRepository extends JpaRepository<ProgramTermVendor, UUID>, ProgramTermVendorRepositoryCustom {
    @Modifying
    @Query("update ProgramTermVendor v SET v.isDelete = true where v.programTerm.program.portal.id = :portalId")
    void deleteByPortalId(UUID portalId);

    @Query(
        "select v.id as id, cp.name as name, v.vendorId as vendorId, " +
        " case when (coalesce(v.calculatedHours, 0) - coalesce(v.allocatedHours, 0)) >= 0 then coalesce(v.calculatedHours, 0) - coalesce(v.allocatedHours, 0) else 0 end as calculatedHours " +
        "from ProgramTermVendor v join CommunityPartner cp on cp.id = v.vendorId " +
        " join v.programTerm t" +
        " where v.status = 'ACTIVE' and  t.id = :programTermId"
    )
    List<IResponseTermVendor> findAllByProgramTermId(UUID programTermId);

    @Query(
        value = """
         SELECT  COALESCE(sum(vendor.vendorBudget), 0) AS totalBudget,
                COALESCE(sum(vendor.assignedBudget), 0) AS assigned,
                COALESCE(sum(vendor.budgetInProgress), 0) AS inProgress,
                COALESCE(sum(vendor.budgetCompleted), 0) AS complete
         FROM program_term pt
         INNER JOIN program pro ON pro.id = pt.program_id
         AND pt.is_delete IS FALSE
         AND pt.is_current IS TRUE
         LEFT JOIN
           (SELECT pt.id AS program_term_id,
                   pt.budget AS vendorBudget,
                   sum((coalesce(ap.hoursInprogres, 0) + coalesce(ap.hoursCompleted, 0) + coalesce(prj.hoursInprogres, 0) + coalesce(prj.hoursCompleted, 0)) * ptv.contracted_rate) AS assignedBudget,
                   sum((coalesce(ap.hoursInprogres, 0) + coalesce(prj.hoursInprogres, 0)) * ptv.contracted_rate) AS budgetInProgress,
                   sum((coalesce(ap.hoursCompleted, 0) + coalesce(prj.hoursCompleted, 0)) * ptv.contracted_rate) AS budgetCompleted
            FROM program_term_vendor ptv
            INNER JOIN program_term pt ON pt.id = ptv.program_term_id
            AND ptv.is_delete IS FALSE
            AND pt.is_current IS TRUE
            INNER JOIN community_partner cp ON cp.id = ptv.vendor_id
            INNER JOIN program pro ON pro.id = pt.program_id
            AND pt.is_delete IS FALSE
            AND pt.is_current IS TRUE
            LEFT JOIN
              (SELECT tas.assign_vendor_id AS assign_vendor_id,
                      count(DISTINCT tas.id) AS totalAssigns,
                      sum(CASE
                              WHEN a.status IN ('SCHEDULED', 'REPORT_REQUIRED', 'OVERDUE') THEN 1
                              ELSE 0
                          END) AS hoursInprogres,
                      sum(CASE
                              WHEN a.status IN ('INVOICED', 'COMPLETED') THEN 1
                              ELSE 0
                          END) AS hoursCompleted
               FROM appointment a
               INNER JOIN technical_assistance_submit tas ON a.technical_assistance_submit_id = tas.id
               AND tas.status NOT IN ('SUBMITTED', 'DENIED')
               INNER JOIN program_term pt ON tas.program_term_id = pt.id
               AND pt.is_current IS TRUE
               GROUP BY tas.assign_vendor_id) AS ap ON ptv.id = ap.assign_vendor_id
            LEFT JOIN
              (SELECT tas.assign_vendor_id AS assign_vendor_id,
                      count(DISTINCT tas.id) AS totalAssigns,
                      sum(CASE
                              WHEN p.status IN ('PROPOSED', 'VENDOR_APPROVED', 'WORK_IN_PROGRESS', 'OVERDUE') THEN p.estimated_hours_needed
                              ELSE 0
                          END) AS hoursInprogres,
                      sum(CASE
                              WHEN p.status IN ('INVOICED', 'COMPLETED') THEN p.estimated_hours_needed
                              ELSE 0
                          END) AS hoursCompleted
               FROM project p
               INNER JOIN technical_assistance_submit tas ON p.technical_assistance_submit_id = tas.id
               AND tas.status NOT IN ('SUBMITTED', 'DENIED')
               INNER JOIN program_term pt ON tas.program_term_id = pt.id
               AND pt.is_current IS TRUE
               GROUP BY tas.assign_vendor_id) AS prj ON ptv.id = prj.assign_vendor_id
            GROUP BY pt.id) AS vendor ON pt.id = vendor.program_term_id
            where (:portalIds is null or pro.portal_id IN (:portalIds))
        """,
        nativeQuery = true
    )
    IResponseCountVendor countByPortalIdAndStatus(List<UUID> portalIds);

    /**
     * Find program term vendor by program term ID and vendor ID.
     */
    ProgramTermVendor findByProgramTermIdAndId(UUID programTermId, UUID vendorId);

    @Query("""
        SELECT ptv
        FROM ProgramTermVendor ptv
        JOIN ptv.programTerm pt on pt.isDelete = false
        JOIN pt.program p on p.isDelete = false
        WHERE ptv.vendorId = :vendorId and p.portal.id = :portalId
            and pt.startDate < :endDate and pt.endDate > :startDate
    """)
    List<ProgramTermVendor> findAllByVendorIdAndDate(UUID vendorId, UUID portalId, Instant startDate, Instant endDate);
}
