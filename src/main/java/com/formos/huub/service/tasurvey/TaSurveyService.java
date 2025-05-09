package com.formos.huub.service.tasurvey;

import com.formos.huub.domain.constant.BusinessConstant;
import com.formos.huub.domain.entity.CommunityPartner;
import com.formos.huub.domain.request.tasurvey.RequestSearchTaSurveyAppointment;
import com.formos.huub.domain.request.tasurvey.RequestSearchTaSurveyProject;
import com.formos.huub.domain.request.tasurvey.RequestSearchTaSurveyTap;
import com.formos.huub.domain.response.tasurvey.IResponseTaSurveyDetailResponse;
import com.formos.huub.domain.response.tasurvey.ResponseTaSurveyOverview;
import com.formos.huub.framework.context.PortalContextHolder;
import com.formos.huub.framework.utils.ObjectUtils;
import com.formos.huub.framework.utils.PageUtils;
import com.formos.huub.repository.AppointmentRepository;
import com.formos.huub.repository.ProjectRepository;
import com.formos.huub.repository.UserRepository;
import com.formos.huub.security.AuthoritiesConstants;
import com.formos.huub.security.SecurityUtils;
import com.formos.huub.service.applicationmanagement.ApplicationManagementService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TaSurveyService {

    AppointmentRepository appointmentRepository;
    ProjectRepository projectRepository;
    UserRepository userRepository;
    ApplicationManagementService applicationManagementService;

    public ResponseTaSurveyOverview getTaSurveyOverview(UUID portalId) {
        Float averageAdvisor;
        Float averageProject;
        if (SecurityUtils.hasCurrentUserNoneOfAuthorities(AuthoritiesConstants.SYSTEM_ADMINISTRATOR, AuthoritiesConstants.PORTAL_HOST, AuthoritiesConstants.COMMUNITY_PARTNER)) {
            throw new AccessDeniedException("Role Member");
        }

        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.COMMUNITY_PARTNER)) {
            var communityPartnerId = getCurrentCommunityPartnerId();
            averageAdvisor = appointmentRepository.getAvgRatingByCommunityPartnerId(communityPartnerId)
                .orElse(null);
            averageProject = projectRepository.getAvgRatingByCommunityPartnerId(communityPartnerId)
                .orElse(null);
        } else {
            if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.PORTAL_HOST)) {
                portalId = PortalContextHolder.getPortalId();
            }
            averageAdvisor = appointmentRepository.getAvgRatingByPortal(portalId)
                .orElse(null);
            averageProject = projectRepository.getAvgRatingByPortal(portalId)
                .orElse(null);
        }

        return ResponseTaSurveyOverview
            .builder()
            .averageAdvisor(averageAdvisor)
            .averageProject(averageProject)
            .averageTapSurvey(null) // tbd
            .build();
    }

    public Map<String, Object> getTaSurveyAppointment(RequestSearchTaSurveyAppointment request) {
        var sort = !ObjectUtils.isEmpty(request.getSort()) ? request.getSort() : "a.appointment_date,desc";
        var pageable = PageRequest.of(request.getPage(), request.getSize(), PageUtils.createSort(sort));

        request.setPortalIds(applicationManagementService.getListPortalByRole(request.getPortalId()));

        HashMap<String, String> sortMap = new HashMap<>();
        sortMap.put("businessOwnerName", "u.normalized_full_name");
        sortMap.put("advisorName", "tau.normalized_full_name");
        sortMap.put("vendorName", "cp.name");
        sortMap.put("appointmentDate", "a.appointment_date");
        sortMap.put("rating", "ad.rating");
        sortMap.put("feedback", "ad.feedback");
        sortMap.put(BusinessConstant.TIMEZONE_KEY, request.getTimezone());
        request.setSearchConditions(ObjectUtils.convertSortParams(request.getSearchConditions(), sortMap));

        request.setCommunityPartnerId(getCurrentCommunityPartnerId());
        var data = appointmentRepository.getRatingResponse(request, pageable);
        return PageUtils.toPage(data);
    }

    public Map<String, Object> getTaSurveyProject(RequestSearchTaSurveyProject request) {
        var sort = !ObjectUtils.isEmpty(request.getSort()) ? request.getSort() : "pr.project_name,asc";
        var pageable = PageRequest.of(request.getPage(), request.getSize(), PageUtils.createSort(sort));

        request.setPortalIds(applicationManagementService.getListPortalByRole(request.getPortalId()));

        HashMap<String, String> sortMap = new HashMap<>();
        sortMap.put("businessOwnerName", "bou.normalized_full_name");
        sortMap.put("advisorName", "tau.normalized_full_name");
        sortMap.put("vendorName", "cp.name");
        sortMap.put("projectName", "pr.project_name");
        sortMap.put("rating", "pr.rating");
        sortMap.put("feedback", "pr.feedback");
        request.setSearchConditions(ObjectUtils.convertSortParams(request.getSearchConditions(), sortMap));

        request.setCommunityPartnerId(getCurrentCommunityPartnerId());
        var data = projectRepository.getRatingResponse(request, pageable);
        return PageUtils.toPage(data);
    }

    public Map<String, Object> getTaSurveyTap(RequestSearchTaSurveyTap request) {
        return null;
    }

    public IResponseTaSurveyDetailResponse getTaSurveyAppointmentDetail(UUID appointmentId) {
        return null;
    }

    public IResponseTaSurveyDetailResponse getTaSurveyProjectDetail(UUID projectId) {
        return null;
    }

    public IResponseTaSurveyDetailResponse getTaSurveyTapDetail(UUID tapId) {
        return null;
    }

    private UUID getCurrentCommunityPartnerId() {
        if (!SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.COMMUNITY_PARTNER)) {
            return null;
        }
        var currentUser = SecurityUtils.getCurrentUser(userRepository);
        return Optional.ofNullable(currentUser.getCommunityPartner()).map(CommunityPartner::getId).orElse(null);
    }
}
