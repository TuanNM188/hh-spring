package com.formos.huub.service.project;

import com.formos.huub.domain.entity.*;
import com.formos.huub.domain.enums.ProjectExtensionRequestStatus;
import com.formos.huub.domain.enums.StatusEnum;
import com.formos.huub.domain.provider.UserProvider;
import com.formos.huub.domain.request.project.RequestCreateProjectExtension;
import com.formos.huub.domain.request.project.RequestExtensionRequestProject;
import com.formos.huub.domain.response.portals.ResponseProgramTermDateRange;
import com.formos.huub.domain.response.project.ResponseProjectExtensionRequest;
import com.formos.huub.framework.enums.DateTimeFormat;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.exception.NotFoundException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.utils.DateUtils;
import com.formos.huub.mapper.project.ProjectExtensionRequestMapper;
import com.formos.huub.repository.*;
import com.formos.huub.security.SecurityUtils;
import com.formos.huub.service.invite.InviteService;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectExtensionRequestService {

    private static final String PROJECT_UPDATE_TEMPLATE = "Extension %s by %s %s";

    private static final long MAX_PROJECT_EXTENSION_REQUESTS = 3;

    private final ProjectExtensionRequestRepository extensionRequestRepository;

    private final ProjectRepository projectRepository;

    private final ProjectUpdateRepository projectUpdateRepository;

    private final ProgramTermRepository programTermRepository;

    private final UserRepository userRepository;

    private final InviteService inviteService;

    private final ProjectExtensionRequestMapper extensionRequestMapper;

    /**
     * Create Extension Request
     *
     * @param request RequestCreateProjectExtension
     * @return ResponseProjectExtensionRequest
     */
    public ResponseProjectExtensionRequest createExtensionRequest(RequestCreateProjectExtension request) {
        Project project = getProjectById(request.getProjectId());

        // Validate extension request
        // 1. Must be after the current completion date.
        // 2. Must fall within the program term dates.
        // 3. Each project can have up to three extension requests
        validateExtensionRequest(request.getNewCompletionDate(), project);
        ProjectExtensionRequest entity = extensionRequestMapper.toEntity(request);
        ProjectExtensionRequest savedRequest = extensionRequestRepository.save(entity);

        User currentUser = SecurityUtils.getCurrentUser(userRepository);
        String updateDescription = String.format(
            PROJECT_UPDATE_TEMPLATE,
            "requested",
            currentUser.getFirstName(),
            currentUser.getLastName()
        );

        // Adds Project Update
        addProjectUpdate(request.getProjectId(), currentUser, updateDescription);

        // Send Email to navigator:
        notifyNavigator(project, currentUser);

        return extensionRequestMapper.toResponse(savedRequest);
    }

    /**
     * Find By Project Id
     *
     * @param projectId UUID
     * @return ResponseProjectExtensionRequest
     */
    public ResponseProjectExtensionRequest findByProjectId(UUID projectId) {
        return extensionRequestMapper.toResponse(
            extensionRequestRepository.findByProjectIdAndStatus(projectId, ProjectExtensionRequestStatus.PROPOSED).orElse(null)
        );
    }

    /**
     * Get Program Term Date Range
     *
     * @param projectId UUID
     * @return ResponseProgramTermDateRange
     */
    public ResponseProgramTermDateRange getProgramTermDateRange(UUID projectId) {
        Project project = getProjectById(projectId);

        return programTermRepository
            .getByPortalId(project.getPortal().getId(), StatusEnum.ACTIVE)
            .map(term -> {
                ResponseProgramTermDateRange response = new ResponseProgramTermDateRange();
                response.setStartDate(term.getStartDate());
                response.setEndDate(term.getEndDate());
                return response;
            })
            .orElse(null);
    }

    /**
     * Approve Extension Request
     *
     * @param request RequestExtensionRequestProject
     */
    public void approveExtensionRequest(RequestExtensionRequestProject request) {
        ProjectExtensionRequest extensionRequest = getExtensionRequestById(request.getId());
        Project project = extensionRequest.getProject();

        updateExtensionRequestStatus(extensionRequest, ProjectExtensionRequestStatus.APPROVED);
        updateProjectCompletionDate(project, extensionRequest.getNewCompletionDate());

        User currentUser = SecurityUtils.getCurrentUser(userRepository);
        String updateDescription = String.format(
            PROJECT_UPDATE_TEMPLATE,
            "approved",
            currentUser.getFirstName(),
            currentUser.getLastName()
        );
        addProjectUpdate(project.getId(), currentUser, updateDescription);

        notifyApproval(project, extensionRequest);
    }

    /**
     * Deny Extension Request
     *
     * @param request RequestExtensionRequestProject
     */
    public void denyExtensionRequest(RequestExtensionRequestProject request) {
        ProjectExtensionRequest extensionRequest = getExtensionRequestById(request.getId());
        Project project = extensionRequest.getProject();

        updateExtensionRequestStatus(extensionRequest, ProjectExtensionRequestStatus.DENIED);

        User currentUser = SecurityUtils.getCurrentUser(userRepository);
        String updateDescription = String.format(PROJECT_UPDATE_TEMPLATE, "denied", currentUser.getFirstName(), currentUser.getLastName());
        addProjectUpdate(project.getId(), currentUser, updateDescription);

        // Email to business owner & advisor deny
        notifyDenial(project);
    }

    /**
     * Add Project Update
     *
     * @param projectId   UUID
     * @param currentUser User
     * @param description String
     */
    private void addProjectUpdate(UUID projectId, User currentUser, String description) {
        Project project = new Project();
        project.setId(projectId);

        ProjectUpdate projectUpdate = new ProjectUpdate();
        projectUpdate.setProject(project);
        projectUpdate.setUserId(currentUser.getId());
        projectUpdate.setDescription(description);

        projectUpdateRepository.save(projectUpdate);
    }

    /**
     * Get Project by Id
     *
     * @param projectId UUID
     * @return Project
     */
    private Project getProjectById(UUID projectId) {
        return projectRepository
            .findById(projectId)
            .orElseThrow(() -> new NotFoundException(MessageHelper.getMessage(Message.Keys.E0010, "Project")));
    }

    private void validateExtensionRequest(Instant newCompletionDate, Project project) {
        validateNewCompletionDate(newCompletionDate, project);
        validateMaxExtensionRequests(project.getId());
    }

    /**
     * Validate new completion date
     *
     * @param newCompletionDate Instant
     * @param project           Project
     */
    private void validateNewCompletionDate(Instant newCompletionDate, Project project) {
        programTermRepository.getByPortalId(project.getPortal().getId(), StatusEnum.ACTIVE).ifPresent(activeTerm -> {
            if (newCompletionDate.isBefore(activeTerm.getStartDate()) || newCompletionDate.isAfter(activeTerm.getEndDate())) {
                throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0080));
            }
        });

        if (newCompletionDate.isBefore(project.getEstimatedCompletionDate())) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0080));
        }
    }

    /**
     * Validate max extension requests
     *
     * @param projectId UUID
     */
    private void validateMaxExtensionRequests(UUID projectId) {
        if (extensionRequestRepository.countByProject_id(projectId) >= MAX_PROJECT_EXTENSION_REQUESTS) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0081));
        }
    }

    /**
     * Notify navigator
     *
     * @param project    Project
     * @param currentUser User
     */
    private void notifyNavigator(Project project, User currentUser) {
        Optional.ofNullable(project.getVendor())
            .flatMap(vendor -> userRepository.findCommunityPartnerNavigatorById(vendor.getId()))
            .ifPresent(
                navigator ->
                    inviteService.sendExtensionRequest(
                        navigator.getEmail(),
                        project.getProjectName(),
                        currentUser.getNormalizedFullName(),
                        navigator.getFirstName(),
                        project
                    )
            );
    }

    /**
     * Get Extension Request by Id
     *
     * @param id UUID
     * @return ProjectExtensionRequest
     */
    private ProjectExtensionRequest getExtensionRequestById(UUID id) {
        return extensionRequestRepository
            .findById(id)
            .orElseThrow(() -> new NotFoundException(MessageHelper.getMessage(Message.Keys.E0010, "Project Extension Request")));
    }

    /**
     * Update Extension Request Status
     *
     * @param request ProjectExtensionRequest
     * @param status  ProjectExtensionRequestStatus
     */
    private void updateExtensionRequestStatus(ProjectExtensionRequest request, ProjectExtensionRequestStatus status) {
        request.setStatus(status);
        extensionRequestRepository.save(request);
    }

    /**
     * Update Project Completion Date
     *
     * @param project  Project
     * @param newDate  Instant
     */
    private void updateProjectCompletionDate(Project project, Instant newDate) {
        project.setEstimatedCompletionDate(newDate);
        projectRepository.save(project);
    }

    /**
     * Notify Approval
     *
     * @param project         Project
     * @param extensionRequest ProjectExtensionRequest
     */
    private void notifyApproval(Project project, ProjectExtensionRequest extensionRequest) {
        String newDate = DateUtils.convertInstantToStringTime(extensionRequest.getNewCompletionDate(), DateTimeFormat.MM_DD_YYYY_DASH);

        // Email to business owner
        Optional.ofNullable(project.getBusinessOwner())
            .map(BusinessOwner::getUser)
            .ifPresent(owner -> sendApprovalEmail(owner, project.getProjectName(), newDate, project, true));

        // Email to advisor approved
        Optional.ofNullable(project.getTechnicalAdvisor())
            .map(TechnicalAdvisor::getUser)
            .ifPresent(advisor -> sendApprovalEmail(advisor, project.getProjectName(), newDate, project, false));
    }

    /**
     * Send Approval Email
     *
     * @param user       User
     * @param projectName String
     * @param newDate    String
     */
    private void sendApprovalEmail(User user, String projectName, String newDate, Project project, boolean isBusinessOwner) {
        inviteService.sendApproveExtensionRequest(user.getEmail(), projectName, user.getNormalizedFullName(), newDate, project, isBusinessOwner);
    }

    /**
     * Notify Denial
     *
     * @param project Project
     */
    private void notifyDenial(Project project) {
        Optional.ofNullable(project.getVendor())
            .flatMap(vendor -> userRepository.findCommunityPartnerNavigatorById(vendor.getId()))
            .ifPresent(navigator -> {
                notifyUserOfDenial(project.getBusinessOwner(), project, navigator, true);
                notifyUserOfDenial(project.getTechnicalAdvisor(), project, navigator, false);
            });
    }

    /**
     * Notify User of Denial
     *
     * @param entity    UserProvider
     * @param project   Project
     * @param navigator User
     */
    private void notifyUserOfDenial(UserProvider entity, Project project, User navigator, boolean isBusinessOwner) {
        // Send Email to business owner and advisor approved
        Optional.ofNullable(entity)
            .map(UserProvider::getUser)
            .ifPresent(
                user ->
                    inviteService.sendDenyExtensionRequest(
                        user.getEmail(),
                        project.getProjectName(),
                        user.getNormalizedFullName(),
                        navigator.getEmail(),
                        project,
                        isBusinessOwner
                    )
            );
    }
}
