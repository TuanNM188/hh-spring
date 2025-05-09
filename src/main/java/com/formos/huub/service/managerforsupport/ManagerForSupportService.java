package com.formos.huub.service.managerforsupport;

import com.formos.huub.domain.enums.ApprovalStatusEnum;
import com.formos.huub.domain.request.project.RequestSearchProjectForBO;
import com.formos.huub.domain.response.advisementcategory.ResponseOverviewBOManager;
import com.formos.huub.domain.response.appointment.IResponseAppointmentUpcoming;
import com.formos.huub.domain.response.technicalassistance.ResponseTechnicalAssistanceRemainHours;
import com.formos.huub.framework.context.PortalContextHolder;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.utils.ObjectUtils;
import com.formos.huub.framework.utils.PageUtils;
import com.formos.huub.repository.*;
import com.formos.huub.security.AuthoritiesConstants;
import com.formos.huub.security.SecurityUtils;
import com.formos.huub.service.portals.PortalFormService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ManagerForSupportService {

    PortalFormService portalFormService;
    UserRepository userRepository;
    TechnicalAssistanceSubmitRepository technicalAssistanceSubmitRepository;
    TechnicalAdvisorRepository technicalAdvisorRepository;
    AppointmentRepository appointmentRepository;
    ProjectRepository projectRepository;

    public ResponseOverviewBOManager getOverviewBoManager() {
        if (!SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.BUSINESS_OWNER)){
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0006));
        }
        var businessOwner = SecurityUtils.getCurrentUser(userRepository);
        UUID portalId = PortalContextHolder.getPortalId();
        var currentTerm = portalFormService.getCurrentTerm(portalId);
        if (currentTerm == null) {
            return null;
        }
        var technicalAssistanceSubmitOpt = technicalAssistanceSubmitRepository.findByPortalIdAndUserIdAndStatus(currentTerm.getId(), businessOwner.getId(), List.of(ApprovalStatusEnum.VENDOR_ASSIGNED.getValue(), ApprovalStatusEnum.APPROVED.getValue()));
        if (technicalAssistanceSubmitOpt.isEmpty()) {
            return null;
        }
        var technicalAssistanceSubmit = technicalAssistanceSubmitOpt.get();
        var remainingHour = ResponseTechnicalAssistanceRemainHours.builder()
            .remainingAwardHours(technicalAssistanceSubmit.getRemainingAwardHours())
            .assignAwardHours(technicalAssistanceSubmit.getAssignAwardHours())
            .build();

        return ResponseOverviewBOManager.builder()
            .remainingHour(remainingHour)
            .recommendAdvisors(technicalAdvisorRepository.getRecommendAdvisor(technicalAssistanceSubmit.getId()))
            .assignNavigator(technicalAssistanceSubmitRepository.getAssignNavigator(technicalAssistanceSubmit.getId()).orElse(null))
            .build();
    }

    public IResponseAppointmentUpcoming getFirstAppointmentUpcoming() {
        if (!SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.BUSINESS_OWNER)){
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0006));
        }
        var businessOwner = SecurityUtils.getCurrentUser(userRepository);
        UUID portalId = PortalContextHolder.getPortalId();
        return appointmentRepository.findFirstAppointmentUpcomingByUserIdAndPortalId(businessOwner.getId(), portalId, Instant.now());
    }

    public Map<String, Object> getAppointmentUpcoming(UUID ignoreAppointmentId, Pageable pageable) {
        if (!SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.BUSINESS_OWNER)){
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0006));
        }
        var businessOwner = SecurityUtils.getCurrentUser(userRepository);
        UUID portalId = PortalContextHolder.getPortalId();
        var result = appointmentRepository.findAppointmentUpcomingByUserIdAndPortalId(businessOwner.getId(), portalId, ignoreAppointmentId, Instant.now(), pageable);
        return PageUtils.toPage(result);
    }

    public Map<String, Object> getProjectForBusinessOwner(RequestSearchProjectForBO request) {
        if (!SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.BUSINESS_OWNER)){
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0006));
        }
        var businessOwner = SecurityUtils.getCurrentUser(userRepository);
        var sort = !ObjectUtils.isEmpty(request.getSort()) ? request.getSort() : "pr.createdDate,desc";
        var pageable = PageRequest.of(request.getPage(), request.getSize(), PageUtils.createSort(sort));

        HashMap<String, String> sortMap = new HashMap<>();
        sortMap.put("estimatedCompletionDate", "pr.estimatedCompletionDate");
        sortMap.put("projectName", "pr.projectName");
        sortMap.put("technicalAdvisorName", "tau.normalizedFullName");
        sortMap.put("status", "pr.status");

        request.setSearchConditions(ObjectUtils.convertSortParams(request.getSearchConditions(), sortMap));

        request.setUserId(businessOwner.getId());
        var data = projectRepository.searchProjectForBusinessOwner(request, pageable);
        return PageUtils.toPage(data);
    }

}
