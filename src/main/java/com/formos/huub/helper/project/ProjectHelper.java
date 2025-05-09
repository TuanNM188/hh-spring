/**
 * ***************************************************
 * * Description :
 * * File        : ProjectHelper
 * * Author      : Hung Tran
 * * Date        : Feb 06, 2025
 * ***************************************************
 **/
package com.formos.huub.helper.project;

import com.formos.huub.domain.entity.CommunityPartner;
import com.formos.huub.domain.entity.Project;
import com.formos.huub.domain.entity.TechnicalAdvisor;
import com.formos.huub.domain.entity.TechnicalAssistanceSubmit;
import com.formos.huub.domain.enums.ApprovalStatusEnum;
import com.formos.huub.domain.response.project.IResponseCountProject;
import com.formos.huub.framework.context.PortalContextHolder;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.exception.NotFoundException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.utils.DateUtils;
import com.formos.huub.repository.ProjectRepository;
import com.formos.huub.repository.TechnicalAssistanceSubmitRepository;
import com.formos.huub.repository.UserRepository;
import com.formos.huub.security.AuthoritiesConstants;
import com.formos.huub.security.SecurityUtils;
import com.formos.huub.service.portals.PortalFormService;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectHelper {

    ProjectRepository projectRepository;
    PortalFormService portalFormService;
    TechnicalAssistanceSubmitRepository technicalAssistanceSubmitRepository;
    UserRepository userRepository;

    /**
     * Find an entity by ID
     * @param repository JpaRepository
     * @param id ID
     * @param entityName Entity name
     * @return Entity
     * @param <T> Entity type
     */
    public <T> T findEntityById(JpaRepository<T, UUID> repository, UUID id, String entityName) {
        return repository.findById(id).orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, entityName)));
    }

    /**
     * Find an entity by ID and Portal ID
     * @param finder Finder function
     * @param id ID
     * @param portalId Portal ID
     * @param entityName Entity name
     * @return Entity
     * @param <T> Entity type
     */
    public <T> T findEntityByIdAndPortalId(BiFunction<UUID, UUID, Optional<T>> finder, UUID id, UUID portalId, String entityName) {
        return finder
            .apply(id, portalId)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, entityName)));
    }

    /**
     * Find an entity by ID and Community Partner ID
     * @param finder Finder function
     * @param id ID
     * @param partnerId Partner ID
     * @param entityName Entity name
     * @return Entity
     * @param <T> Entity type
     */
    public <T> T findEntityByIdAndCommunityPartnerId(
        BiFunction<UUID, UUID, Optional<T>> finder,
        UUID id,
        UUID partnerId,
        String entityName
    ) {
        return finder
            .apply(id, partnerId)
            .orElseThrow(() -> new NotFoundException(MessageHelper.getMessage(Message.Keys.E0010, entityName)));
    }

    /**
     * Validate the maximum number of files
     * @param currentNumberOfFiles current number of files request
     * @param maxFiles max files
     * @param fieldName field name
     */
    public void validateMaxFiles(Integer currentNumberOfFiles, Integer maxFiles, String fieldName) {
        if (currentNumberOfFiles > maxFiles) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0038, fieldName, maxFiles));
        }
    }

    /**
     * Validate Estimated Completion Date
     * @param proposedStartDate Proposed start date
     * @param estimatedCompletionDate Estimated completion date
     */
    public void validateCompletionDate(Instant proposedStartDate, Instant estimatedCompletionDate) {
        if (!DateUtils.isBeforeOrEqualDate(proposedStartDate, estimatedCompletionDate)) {
            throw new IllegalArgumentException("Estimated completion date must be after proposed start date");
        }
    }

    /**
     *
     * @param estimatedHoursNeeded
     * @param remainingAwardHours
     */
    public void validateEstimatedHoursNeeded(Float estimatedHoursNeeded, Float remainingAwardHours) {
        if (estimatedHoursNeeded > remainingAwardHours) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0092));
        }
    }

    /**
     * Validate Estimated Hours Needed
     * @param estimatedHoursNeeded Estimated hours needed
     */
    public void validateEstimatedHoursNeeded(Float estimatedHoursNeeded) {
        if (Objects.nonNull(estimatedHoursNeeded) && estimatedHoursNeeded % 0.5 != 0) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0082));
        }
    }

    /**
     * Validate Project Name Uniqueness
     * @param projectName Project name
     */
    public void validateProjectNameUniqueness(String projectName) {
        if (projectRepository.existsByProjectName(projectName)) {
            throw new IllegalArgumentException("Project name is duplicated");
        }
    }

    /**
     * @param currentSize current size of list service outcomes
     */
    public void validateRequireServiceOutcomes(Integer currentSize, String fieldName) {
        if (currentSize <= 0) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0027, fieldName));
        }
    }

    /**
     * @param currentHoursCompleted current Hours Completed
     * @param maxHoursCompleted     max Hours Completed
     */
    public void validateHoursCompleted(Integer currentHoursCompleted, Float maxHoursCompleted) {
        if (currentHoursCompleted > maxHoursCompleted) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0071));
        }
    }


    /**
     * @param confirmation current confirmation
     */
    public void validateConfirmation(Boolean confirmation, String fieldName) {
        if (!Boolean.TRUE.equals(confirmation)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0072, fieldName));
        }
    }

    /**
     * Get Application For Project
     * @param portalId Portal ID
     * @param applicationId Application ID
     * @return Application For Project
     */
    public TechnicalAssistanceSubmit getApplicationForProject(UUID portalId, UUID applicationId) {
        var currentTerm = portalFormService.getCurrentTerm(portalId);
        if (Objects.isNull(currentTerm)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Current Program Term"));
        }
        // 2. Validate Business Owner existence and association with Portal
        return technicalAssistanceSubmitRepository
            .findByIdAndProgramTermIdAndStatus(applicationId, currentTerm.getId(), ApprovalStatusEnum.APPROVED)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0015, "Application")));
    }

    /**
     * Get Overview Count Project
     * @return Overview Count Project
     */
    public IResponseCountProject getOverviewCountProject(List<UUID> portalIds) {

        var communityPartnerId = getCurrentCommunityPartnerId();
        var technicalAdvisorId = getCurrentTechnicalAdvisorId();
        return projectRepository.countByPortalIdAndStatus(portalIds, communityPartnerId, technicalAdvisorId);
    }

    /**
     * Get current Community Partner ID
     * @return Community Partner ID
     */
    public UUID getCurrentCommunityPartnerId() {
        if (!SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.COMMUNITY_PARTNER)) {
            return null;
        }
        var currentUser = SecurityUtils.getCurrentUser(userRepository);
        return Optional.ofNullable(currentUser.getCommunityPartner()).map(CommunityPartner::getId).orElse(null);
    }

    /**
     * Get current Technical Advisor ID
     * @return Technical Advisor ID
     */
    public UUID getCurrentTechnicalAdvisorId() {
        if (!SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.TECHNICAL_ADVISOR)) {
            return null;
        }
        var currentUser = SecurityUtils.getCurrentUser(userRepository);
        return Optional.ofNullable(currentUser.getTechnicalAdvisor()).map(TechnicalAdvisor::getId).orElse(null);
    }
}
