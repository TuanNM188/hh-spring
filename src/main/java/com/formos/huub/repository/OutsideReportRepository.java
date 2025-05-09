package com.formos.huub.repository;

import com.formos.huub.domain.entity.OutsideReport;
import com.formos.huub.domain.request.outsidereport.RequestSearchOutsideReport;
import com.formos.huub.domain.response.report.IResponseSearchReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OutsideReportRepository extends JpaRepository<OutsideReport, UUID> {

    @Query("""
        SELECT
            r.id AS id,
            r.month AS month,
            r.year AS year,
            r.summary AS summary,
            r.pdfUrl AS pdfUrl,
            p.id AS portalId,
            p.platformName AS platformName
        FROM
            OutsideReport r
        JOIN
            r.portal p
        WHERE
            (:#{#cond.portalId} IS NULL OR p.id = :#{#cond.portalId})
            AND (:#{#cond.year} IS NULL OR r.year = :#{#cond.year})
    """)
    Page<IResponseSearchReport> searchOutsideReportByConditions(@Param("cond") RequestSearchOutsideReport request, Pageable pageable);
}
