package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.enums.ReportExportTypeEnum;
import com.formos.huub.domain.request.metricsreport.RequestSearchAppointmentProjectReport;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.framework.utils.HeaderUtils;
import com.formos.huub.framework.utils.UUIDUtils;
import com.formos.huub.service.metricsreport.MetricsReportService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reports")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MetricsReportController {

    MetricsReportService metricsReportService;

    ResponseSupport responseSupport;

    @PostMapping("/appointment-project/search")
    @PreAuthorize("hasPermission(null, 'SEARCH_APPOINTMENT_PROJECT_REPORTS')")
    public ResponseEntity<ResponseData> searchAppointmentProjectReports(
        @Valid @RequestBody RequestSearchAppointmentProjectReport request, HttpServletRequest httpServletRequest) {
        request.setTimezone(HeaderUtils.getTimezone(httpServletRequest));
        return responseSupport.success(
            ResponseData.builder().data(metricsReportService.searchAppointmentProjectReports(request)).build()
        );
    }

    @PostMapping("/invoice-advisor/search")
    @PreAuthorize("hasPermission(null, 'SEARCH_INVOICE_AMOUNT_BY_ADVISORS')")
    public ResponseEntity<ResponseData> searchInvoiceByAdvisor(
        @Valid @RequestBody RequestSearchAppointmentProjectReport request, HttpServletRequest httpServletRequest) {
        request.setTimezone(HeaderUtils.getTimezone(httpServletRequest));
        var response = metricsReportService.searchInvoiceAmountByAdvisor(request);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @GetMapping("/appointment-project/overview")
    @PreAuthorize("hasPermission(null, 'SEARCH_APPOINTMENT_PROJECT_INVOICE_OVERVIEW')")
    public ResponseEntity<ResponseData> technicalAssistanceOverview(
        String portalId, String startDate, String endDate, HttpServletRequest httpServletRequest) {
        var timezone = HeaderUtils.getTimezone(httpServletRequest);
        var response = metricsReportService.getOverviewApplicationData(UUIDUtils.toUUID(portalId), startDate, endDate, timezone);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @GetMapping("/metabase-token")
    @PreAuthorize("hasPermission(null, 'GENERATE_IFRAME_URL_METABASE')")
    public ResponseEntity<ResponseData> getMetabaseTokenByDashboard(Integer dashboard, String portalId, HttpServletRequest httpServletRequest) {
        var timezone = HeaderUtils.getTimezone(httpServletRequest);
        var response = metricsReportService.generateTokenMetabaseForPortalHost(dashboard, portalId, timezone);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @GetMapping("/export")
    @PreAuthorize("hasPermission(null, 'EXPORT_METRIC_REPORT_CSV')")
    public void exportCsv(HttpServletResponse response, ReportExportTypeEnum reportExportType,
                          String portalId, String startDate, String endDate, HttpServletRequest httpServletRequest) {
        var timezone = HeaderUtils.getTimezone(httpServletRequest);
        metricsReportService.exportReportByType(response, reportExportType, UUIDUtils.toUUID(portalId),
            startDate, endDate, timezone);
    }

}
