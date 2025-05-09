/**
 * ***************************************************
 * * Description :
 * * File        : OwnerCheckSecurity
 * * Author      : Hung Tran
 * * Date        : Oct 11, 2024
 * ***************************************************
 **/
package com.formos.huub.security;

import com.formos.huub.domain.entity.LearningLibrary;
import com.formos.huub.domain.entity.User;
import com.formos.huub.domain.enums.LearningLibraryStatusEnum;
import com.formos.huub.framework.context.PortalContextHolder;
import com.formos.huub.repository.*;
import java.util.Objects;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("ownerCheckSecurity")
public class OwnerCheckSecurity {

    @Autowired
    private LearningLibraryRepository learningLibraryRepository;

    @Autowired
    private TechnicalAdvisorRepository technicalAdvisorRepository;

    @Autowired
    private CalendarEventRepository calendarEventRepository;

    @Autowired
    private SurveyRepository surveyRepository;

    @Autowired
    private UserRepository userRepository;

    public boolean isLearningLibraryUpdate(String id, Authentication authentication) {
        if (Objects.isNull(id) || Objects.isNull(authentication)) {
            return false;
        }

        String username = authentication.getName();
        UUID learningLibraryId = UUID.fromString(id);
        return learningLibraryRepository.existsByUsername(username, learningLibraryId);
    }

    public boolean isCommunityPartnerOwnerEvent(String id, Authentication authentication) {
        if (Objects.isNull(id) || Objects.isNull(authentication)) {
            return false;
        }
        if (!SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.COMMUNITY_PARTNER)) {
            return false;
        }
        String username = authentication.getName();
        UUID eventId = UUID.fromString(id);
        return calendarEventRepository.existsByIdAndCreatedBy(eventId, username);
    }

    public boolean isCommunityPartnerOwnerNavigator(Authentication authentication) {
        if (Objects.isNull(authentication) || !SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.COMMUNITY_PARTNER)) {
            return false;
        }

        User user = SecurityUtils.getCurrentUser(userRepository);
        return !Objects.isNull(user) && Boolean.TRUE.equals(user.getIsNavigator());
    }

    public boolean isLearningLibraryGetDetail(String id) {
        UUID portalId = getPortalIdFromContext();
        if (Objects.isNull(id) || Objects.isNull(portalId)) {
            return false;
        }

        UUID learningLibraryId = UUID.fromString(id);
        LearningLibrary learningLibrary = learningLibraryRepository.findById(learningLibraryId).orElse(null);

        if (Objects.nonNull(learningLibrary) && SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.BUSINESS_OWNER)) {
            return LearningLibraryStatusEnum.PUBLISHED.equals(learningLibrary.getStatus());
        }

        return learningLibraryRepository.existsByPortalIdAndLearningLibraryId(portalId, learningLibraryId);
    }

    public boolean isPortalUpdate(String id) {
        UUID contextPortalId = getPortalIdFromContext();
        if (Objects.isNull(id) || Objects.isNull(contextPortalId)) {
            return false;
        }

        UUID portalId = UUID.fromString(id);
        return contextPortalId.equals(portalId);
    }

    public boolean isTechnicalAdvisorUpdate(String id) {
        UUID portalId = getPortalIdFromContext();
        if (Objects.isNull(id) || Objects.isNull(portalId)) {
            return false;
        }

        UUID technicalAdvisorId = UUID.fromString(id);
        return technicalAdvisorRepository.existsByPortalIdAndTechnicalAdvisorId(portalId, technicalAdvisorId);
    }

    private UUID getPortalIdFromContext() {
        return PortalContextHolder.getPortalId();
    }

    public boolean isResponseSurveyForm(String id) {
        UUID portalId = getPortalIdFromContext();
        if (Objects.isNull(id) || Objects.isNull(portalId)) {
            return false;
        }
        UUID surveyId = UUID.fromString(id);
        return surveyRepository.existsByPortalId(portalId, surveyId);
    }
}
