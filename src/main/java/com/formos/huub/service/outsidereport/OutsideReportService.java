package com.formos.huub.service.outsidereport;

import com.formos.huub.domain.request.outsidereport.RequestCreateOutsideReport;
import com.formos.huub.domain.request.outsidereport.RequestSearchOutsideReport;
import com.formos.huub.framework.base.BaseService;
import com.formos.huub.framework.context.PortalContextHolder;
import com.formos.huub.framework.exception.NotFoundException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.utils.PageUtils;
import com.formos.huub.framework.utils.UUIDUtils;
import com.formos.huub.mapper.outsidereport.OutsideReportMapper;
import com.formos.huub.repository.OutsideReportRepository;
import com.formos.huub.security.AuthoritiesConstants;
import com.formos.huub.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OutsideReportService extends BaseService {

    OutsideReportMapper outsideReportMapper;
    OutsideReportRepository outsidereportRepository;

    public UUID createOutsideReport(@Valid RequestCreateOutsideReport request) {
        var portalId = UUIDUtils.toUUID(request.getPortalId());
        var report = outsideReportMapper.toEntity(request,portalId);
        outsidereportRepository.save(report);
        return report.getId();
    }

    public void deleteOutsideReport(UUID outsiderReportId) {
        var report = outsidereportRepository
            .findById(outsiderReportId)
            .orElseThrow(() -> new NotFoundException(MessageHelper.getMessage(Message.Keys.E0010, "report")));
        outsidereportRepository.delete(report);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> searchOutsideReportByConditions(RequestSearchOutsideReport request, Pageable pageable) {
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.PORTAL_HOST)) {
            request.setPortalId(PortalContextHolder.getPortalId());
        }
        return PageUtils.toPage(outsidereportRepository.searchOutsideReportByConditions(request, pageable));
    }

}
