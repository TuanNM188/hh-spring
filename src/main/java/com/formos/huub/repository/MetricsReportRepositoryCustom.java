package com.formos.huub.repository;

import com.formos.huub.domain.request.member.RequestSearchMember;
import com.formos.huub.domain.request.metricsreport.RequestSearchAppointmentProjectReport;
import com.formos.huub.domain.response.metricsreport.ResponseInvoicedAmountByAdvisor;
import com.formos.huub.domain.response.metricsreport.ResponseSearchAppointmentProjectReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MetricsReportRepositoryCustom {

    Page<ResponseInvoicedAmountByAdvisor> getAllInvoiceAmountByAdvisorPageable(RequestSearchAppointmentProjectReport request,
                                                                               Pageable pageable);

    Page<ResponseSearchAppointmentProjectReport> searchAppointmentProjectReports(RequestSearchAppointmentProjectReport request,
                                                                                 Pageable pageable);

}
