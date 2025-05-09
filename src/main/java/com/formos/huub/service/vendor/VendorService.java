package com.formos.huub.service.vendor;

import com.formos.huub.domain.request.vendor.RequestSearchVendor;
import com.formos.huub.domain.response.vendor.IResponseCountVendor;
import com.formos.huub.domain.response.vendor.ResponseOverviewVendor;
import com.formos.huub.framework.context.PortalContextHolder;
import com.formos.huub.framework.utils.ObjectUtils;
import com.formos.huub.framework.utils.PageUtils;
import com.formos.huub.repository.ProgramTermVendorRepository;
import com.formos.huub.service.applicationmanagement.ApplicationManagementService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VendorService {

    ProgramTermVendorRepository programTermVendorRepository;

    ApplicationManagementService applicationManagementService;

    public Map<String, Object> searchAllVendorWithProjects(RequestSearchVendor request) {
        String sort = ObjectUtils.isEmpty(request.getSort()) ? "createdDate,desc" : request.getSort();
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), PageUtils.createSort(sort));
        request.setPortalIds(applicationManagementService.getListPortalByRole(request.getPortalId()));
        // Fetch data and map to response DTOs
        var data = programTermVendorRepository.searchByTermAndCondition(request, pageable);

        // Return paginated response
        return PageUtils.toPage(data);
    }

    public ResponseOverviewVendor getOverviewBudgetVendor(UUID portalId, Boolean isPercent) {
        portalId = Objects.nonNull(portalId) ? portalId : PortalContextHolder.getPortalId();
        var portalIds = applicationManagementService.getListPortalByRole(portalId);

        var data = programTermVendorRepository.countByPortalIdAndStatus(portalIds);
        var response = new ResponseOverviewVendor();

        if (Objects.isNull(isPercent) || Boolean.FALSE.equals(isPercent)) {
            BeanUtils.copyProperties(data, response);
            response.setAvailable(data.getTotalBudget().subtract(data.getAssigned()));
            return response;
        }
        return calculatePercentageResponse(data);
    }

    private ResponseOverviewVendor calculatePercentageResponse(IResponseCountVendor data) {
        var response = new ResponseOverviewVendor();
        var totalBudget = data.getTotalBudget();

        if (totalBudget.compareTo(BigDecimal.ZERO) == 0) {
            response.setAvailable(BigDecimal.ZERO);
            response.setAssigned(BigDecimal.ZERO);
            response.setInProgress(BigDecimal.ZERO);
            response.setComplete(BigDecimal.ZERO);
            return response;
        }

        var percent100 = BigDecimal.valueOf(100);
        response.setAvailable(getPercentage(data.getTotalBudget().subtract(data.getAssigned()), totalBudget, percent100));
        response.setAssigned(getPercentage(data.getAssigned(), totalBudget, percent100));
        response.setInProgress(getPercentage(data.getInProgress(), totalBudget, percent100));
        response.setComplete(getPercentage(data.getComplete(), totalBudget, percent100));

        return response;
    }

    private BigDecimal getPercentage(BigDecimal value, BigDecimal total, BigDecimal percent100) {
        if (total == null || total.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return value.divide(total, 10, RoundingMode.HALF_UP)
            .multiply(percent100)
            .setScale(2, RoundingMode.HALF_UP);
    }

}
