package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.request.outsidereport.RequestCreateOutsideReport;
import com.formos.huub.domain.request.outsidereport.RequestSearchOutsideReport;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.framework.validation.constraints.UUIDCheck;
import com.formos.huub.service.outsidereport.OutsideReportService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/outside-report")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OutsideReportController {

    ResponseSupport responseSupport;
    OutsideReportService reportService;

    @PostMapping
    @PreAuthorize("hasPermission(null, 'CREATE_OUTSIDE_REPORT')")
    public ResponseEntity<ResponseData> create(@RequestBody @Valid RequestCreateOutsideReport request) {
        UUID reportId = reportService.createOutsideReport(request);
        return responseSupport.success(ResponseData.builder().data(reportId).build());
    }

    @GetMapping("/search")
    @PreAuthorize("hasPermission(null, 'SEARCH_OUTSIDE_REPORT')")
    public ResponseEntity<ResponseData> getReportWithPageable(
        RequestSearchOutsideReport request,
        @SortDefault.SortDefaults({
            @SortDefault(sort = "year", direction = Sort.Direction.DESC),
            @SortDefault(sort = "month", direction = Sort.Direction.DESC)
    }) Pageable pageable) {
        return responseSupport.success(ResponseData.builder().data(reportService.searchOutsideReportByConditions(request, pageable)).build());
    }

    @DeleteMapping("/{outsideReportId}")
    @PreAuthorize("hasPermission(null, 'DELETE_OUTSIDE_REPORT')")
    public ResponseEntity<ResponseData> delete(@PathVariable @UUIDCheck String outsideReportId) {
        reportService.deleteOutsideReport(UUID.fromString(outsideReportId));
        return responseSupport.success();
    }

}
