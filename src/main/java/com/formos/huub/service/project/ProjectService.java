/**
 * ***************************************************
 * * Description :
 * * File        : ProjectService
 * * Author      : Hung Tran
 * * Date        : Jan 20, 2025
 * ***************************************************
 **/
package com.formos.huub.service.project;

import com.formos.huub.domain.constant.BusinessConstant;
import com.formos.huub.domain.constant.FormConstant;
import com.formos.huub.domain.entity.*;
import com.formos.huub.domain.enums.EntityTypeEnum;
import com.formos.huub.domain.enums.ProjectStatusEnum;
import com.formos.huub.domain.enums.RoleEnum;
import com.formos.huub.domain.request.common.RequestAttachmentFile;
import com.formos.huub.domain.request.project.RequestCreateFeedbackProject;
import com.formos.huub.domain.request.project.RequestCreateProject;
import com.formos.huub.domain.request.project.RequestSearchProject;
import com.formos.huub.domain.request.project.RequestUpdateProject;
import com.formos.huub.domain.response.project.IResponseCountProject;
import com.formos.huub.domain.response.project.ResponseProject;
import com.formos.huub.domain.response.project.ResponseProjectHeader;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.exception.NotFoundException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.utils.ObjectUtils;
import com.formos.huub.framework.utils.PageUtils;
import com.formos.huub.framework.utils.StringUtils;
import com.formos.huub.framework.utils.UUIDUtils;
import com.formos.huub.helper.project.ProjectHelper;
import com.formos.huub.mapper.entityattachment.EntityAttachmentMapper;
import com.formos.huub.mapper.project.ProjectMapper;
import com.formos.huub.repository.*;
import com.formos.huub.security.AuthoritiesConstants;
import com.formos.huub.security.SecurityUtils;
import com.formos.huub.service.applicationmanagement.ApplicationManagementService;
import com.formos.huub.service.invite.InviteService;
import com.formos.huub.service.member.MemberService;
import com.formos.huub.service.pdf.PdfService;
import com.formos.huub.service.pushnotification.PushNotificationService;
import com.formos.huub.service.useranswerform.UserFormService;
import java.time.Instant;
import java.util.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectService {

    private static final Integer MAX_SECTION_FILES = 8;

    private static final String VALIDATE_PORTAL = "Portal";
    private static final String VALIDATE_BUSINESS_OWNER = "Business Owner";
    private static final String VALIDATE_CATEGORY = "Category";
    private static final String VALIDATE_APPOINTMENT = "Appointment";
    private static final String VALIDATE_BUSINESS_OWNER_USER = "Business Owner User";
    private static final String VALIDATE_APPLICATION = "Application";
    private static final String VALIDATE_NUM_OF_FILES = "Num of Files";

    // Group related repositories together
    ProjectRepository projectRepository;
    EntityAttachmentRepository entityAttachmentRepository;

    // Group user-related repositories
    UserRepository userRepository;
    BusinessOwnerRepository businessOwnerRepository;
    TechnicalAdvisorRepository technicalAdvisorRepository;
    TechnicalAssistanceSubmitRepository technicalAssistanceSubmitRepository;

    // Group other repositories
    PortalRepository portalRepository;
    CommunityPartnerRepository communityPartnerRepository;
    CategoryRepository categoryRepository;
    ServiceOfferedRepository serviceOfferedRepository;
    AppointmentRepository appointmentRepository;

    // Group mappers and services
    ProjectMapper projectMapper;
    EntityAttachmentMapper entityAttachmentMapper;
    ProjectHelper projectHelper;
    UserFormService userFormService;
    InviteService inviteService;
    ApplicationManagementService applicationManagementService;
    PushNotificationService pushNotificationService;
    PdfService pdfService;
    MemberService memberService;

    @Getter
    @AllArgsConstructor
    private static class ProjectCreationContext {

        private final Portal portal;
        private final BusinessOwner businessOwner;
        private final CommunityPartner vendor;
        private final Category category;
        private final TechnicalAdvisor assignAdvisor;
        private final TechnicalAssistanceSubmit technicalAssistanceSubmit;
    }

    /**
     * search Appointments by term and condition
     *
     * @param request RequestSearchAppointment
     * @return Map<String, Object> appointment
     */
    public Map<String, Object> searchProjects(RequestSearchProject request) {
        var sort = !ObjectUtils.isEmpty(request.getSort()) ? request.getSort() : "pr.created_date,desc";
        var pageable = PageRequest.of(request.getPage(), request.getSize(), PageUtils.createSort(sort));

        request.setPortalIds( applicationManagementService.getListPortalByRole(request.getPortalId(), true));
        HashMap<String, String> sortMap = new HashMap<>();
        sortMap.put("projectName", "pr.project_name");
        sortMap.put("estimatedHoursNeeded", "pr.estimated_hours_needed");
        sortMap.put("completedDate", "pr.completed_date");
        sortMap.put("businessOwnerName", "bou.normalized_full_name");
        sortMap.put("advisorName", "tau.normalized_full_name");
        sortMap.put("navigatorName", "una.normalized_full_name");
        sortMap.put("status", "pr.status");
        sortMap.put("createdDate", "pr.created_date");
        sortMap.put("requestDate", "pr.created_date");
        sortMap.put("estimatedCompletionDate", "pr.estimated_completion_date");
        sortMap.put(BusinessConstant.TIMEZONE_KEY, request.getTimezone());
        request.setSearchConditions(ObjectUtils.convertSortParams(request.getSearchConditions(), sortMap));

        if (!ObjectUtils.isEmpty(request.getStatus())) {
            var status = Arrays.stream(request.getStatus().split(",")).toList();
            request.setProjectStatus(status);
        }
        request.setCommunityPartnerId(projectHelper.getCurrentCommunityPartnerId());
        request.setTechnicalAdvisorId(projectHelper.getCurrentTechnicalAdvisorId());
        if (Objects.isNull(request.getIsCurrent())) {
            request.setIsCurrent(true);
        }
        var data = projectRepository.searchByTermAndCondition(request, pageable);
        return PageUtils.toPage(data);
    }

    public IResponseCountProject countOverviewProject(UUID portalId) {
        var portalIds = applicationManagementService.getListPortalByRole(portalId, true);
        return projectHelper.getOverviewCountProject(portalIds);
    }

    /**
     * Create feedback for project
     *
     * @param request RequestCreateFeedbackProject
     */
    public void createFeedback(RequestCreateFeedbackProject request) {
        // Validate request
        if (request == null || request.getProjectId() == null || StringUtils.isBlank(request.getRating())) {
            throw new IllegalArgumentException("Invalid feedback request");
        }

        // Get current user and validate
        var currentUser = SecurityUtils.getCurrentUser(userRepository);
        if (Objects.isNull(currentUser)) {
            throw new NotFoundException(MessageHelper.getMessage(Message.Keys.E0010, "User"));
        }

        // Get Project and validate
        // Check if project exist for current user (user make project, Business Owner)

        var businessOwner = businessOwnerRepository.findByUserId(currentUser.getId())
            .orElseThrow(() -> new NotFoundException(MessageHelper.getMessage(Message.Keys.E0010, "Business Owner")));

        var project = projectRepository
            .findByIdAndBusinessOwnerId(request.getProjectId(), businessOwner.getId())
            .orElseThrow(() -> new NotFoundException(MessageHelper.getMessage(Message.Keys.E0010, "Project")));

        try {
            // Update project feedback
            project.setFeedback(request.getFeedback());
            project.setWorkAsExpected(request.isWorkAsExpected());
            project.setRating(Math.round(Float.parseFloat(request.getRating())));
            projectRepository.save(project);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid rating format");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to update project feedback", e);
        }
    }

    /**
     * get project entity
     *
     * @param projectId UUID
     */
    private Project getProject(UUID projectId) {
        return projectRepository
            .findById(projectId)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "project")));
    }

    /**
     * get project detail
     *
     * @param projectId UUID
     */
    public ResponseProject getDetail(UUID projectId) {
        var project = getProject(projectId);
        ResponseProject responseProject = projectMapper.toResponse(project);

        var attachments = entityAttachmentRepository.findByEntityIdAndEntityType(responseProject.getId(), EntityTypeEnum.PROJECT);
        responseProject.setAttachments(attachments);

        // Set category name
        Category category = projectHelper.findEntityById(categoryRepository, project.getCategoryId(), "Category");
        responseProject.setCategoryName(category.getName());

        // Set appointment if exists
        appointmentRepository
            .getAppointmentByAdvisorAndBusinessOwner(
                project.getTechnicalAdvisor().getId(),
                project.getBusinessOwner().getUser().getId(),
                project.getProposedStartDate()
            )
            .ifPresent(appointment -> {
                responseProject.setAppointmentStartDate(appointment.getAppointmentDate());
                responseProject.setAppointmentId(appointment.getId());
            });

        return responseProject;
    }

    /**
     * Create new project
     *
     * @param request RequestCreateProject
     */
    public void create(RequestCreateProject request) {
        // 1. Validate all required entities and get context

        ProjectCreationContext context = null;

        if (Objects.nonNull(request.getAppointmentId())) {
            context = validateAndGetEntitiesForAdditionalScopeOfWork(request);
        } else {
            context = validateAndGetEntities(request);
        }

        // 2. Create and save project
        Project project = buildNewProject(request, context);
        Project savedProject = projectRepository.save(project);

        // 3. Save attachments if exist
        saveAttachments(request.getAttachments(), savedProject.getId());

        if (Objects.nonNull(savedProject.getAppointmentId())) {
            pdfService.generatePdfAppointmentReport(savedProject.getAppointmentId());
        }

        // 4. Send email notification to Navigator

        final ProjectCreationContext finalContext = context;

        userRepository
            .findCommunityPartnerNavigatorById(savedProject.getVendor().getId())
            .ifPresent(navigatorUser -> {
                inviteService.sendNewProject(
                    navigatorUser.getEmail(),
                    finalContext.getBusinessOwner().getUser().getNormalizedFullName(),
                    navigatorUser.getFirstName(),
                    finalContext.getAssignAdvisor().getUser().getNormalizedFullName(),
                    savedProject
                );
            });
    }

    /**
     * update project
     *
     * @param request RequestUpdateProject
     */
    public ResponseProject update(RequestUpdateProject request) {
        // 1. Get and validate project
        Project project = projectRepository
            .findById(request.getId())
            .orElseThrow(() -> new NotFoundException(MessageHelper.getMessage(Message.Keys.E0010, "Project")));

        // 2. Validate request
        validateUpdateRequest(request, project);

        // 3. Update project
        updateProjectFields(project, request);

        if (
            Objects.nonNull(project.getRemainingAwardHours()) &&
            (ProjectStatusEnum.VENDOR_APPROVED.equals(project.getStatus()) ||
                ProjectStatusEnum.WORK_IN_PROGRESS.equals(project.getStatus()))
        ) {
            updateRemainingAwardHours(project);
        }

        Project updatedProject = projectRepository.save(project);
        pdfService.generatePdfProjectReport(project.getId());
        // 4. Handle attachments
        var attachments = entityAttachmentRepository.findByEntityIdAndEntityType(updatedProject.getId(), EntityTypeEnum.PROJECT);
        List<EntityAttachment> updatedAttachments = handleUpdateProjectAttachment(project.getId(), request.getAttachments(), attachments)
            .stream()
            .map(entityAttachmentMapper::toResponse)
            .toList();

        // 5. Build response
        ResponseProject responseProject = projectMapper.toResponse(updatedProject);
        responseProject.setAttachments(updatedAttachments);
        return responseProject;
    }

    /**
     * get project header
     *
     * @param projectId UUID
     */
    public ResponseProjectHeader getProjectHeader(UUID projectId) {
        Project project = projectRepository
            .findById(projectId)
            .orElseThrow(() -> new NotFoundException(MessageHelper.getMessage(Message.Keys.E0010, "Project")));

        ResponseProjectHeader response = projectMapper.toResponseProjectHeader(project);

        // Set business name
        response.setBusinessName(getBusinessName(project.getBusinessOwner().getUser()));

        // Set category name if exists
        Optional.ofNullable(projectHelper.findEntityById(categoryRepository, project.getCategoryId(), "Category")).ifPresent(
            category -> response.setCategoryName(category.getName())
        );

        // Set remaining award hours if appointment exists
        setRemainingAwardHours(project, response);

        return response;
    }

    /**
     * @param projectId UUID
     */
    public void approveProject(UUID projectId) {
        Project project = projectRepository
            .findById(projectId)
            .orElseThrow(() -> new NotFoundException(MessageHelper.getMessage(Message.Keys.E0010, "Project")));
        boolean isBusinessOwner = SecurityUtils.hasCurrentUserAnyOfAuthorities(AuthoritiesConstants.BUSINESS_OWNER);

        if (Objects.nonNull(project.getTechnicalAssistanceSubmit()) && !isBusinessOwner) {
            projectHelper.validateEstimatedHoursNeeded(project.getEstimatedHoursNeeded(), project.getTechnicalAssistanceSubmit().getRemainingAwardHours());
        }

        updateApprovedProjectFields(project, isBusinessOwner);

        updateRemainingAwardHours(project);

        BusinessOwner businessOwner = project.getBusinessOwner();
        TechnicalAdvisor technicalAdvisor = project.getTechnicalAdvisor();

        if (Objects.nonNull(businessOwner) && Objects.nonNull(technicalAdvisor)) {
            User businessOwnerUser = businessOwner.getUser();
            User technicalAdvisorUser = technicalAdvisor.getUser();
            Portal portal = project.getPortal();

            //Business Owner receives Email
            userRepository
                .findCommunityPartnerNavigatorById(project.getVendor().getId())
                .ifPresent(navigatorUser -> {
                    if (isBusinessOwner) {
                        inviteService.sendProjectProposalApprovedForBusinessOwner(
                            technicalAdvisorUser.getEmail(),
                            businessOwnerUser.getNormalizedFullName(),
                            technicalAdvisorUser.getFirstName(),
                            navigatorUser.getNormalizedFullName(),
                            navigatorUser.getEmail(),
                            project
                        );
                    } else {
                        inviteService.sendApproveProject(
                            businessOwnerUser.getEmail(),
                            technicalAdvisorUser.getNormalizedFullName(),
                            businessOwnerUser.getFirstName(),
                            navigatorUser.getNormalizedFullName(),
                            navigatorUser.getEmail(),
                            project
                        );
                    }
                });
            if (!isBusinessOwner) {
                //Business Owner receives SMS
                pushNotificationService.sendSmsProjectNotApprovedForBusinessOwner(
                    businessOwnerUser,
                    technicalAdvisorUser.getNormalizedFullName(),
                    portal.getPlatformName()
                );
            }
        }
    }

    /**
     * @param projectId UUID
     */
    public void denyProject(UUID projectId) {
        Project project = projectRepository
            .findById(projectId)
            .orElseThrow(() -> new NotFoundException(MessageHelper.getMessage(Message.Keys.E0010, "Project")));

        TechnicalAssistanceSubmit technicalAssistanceSubmit = project.getTechnicalAssistanceSubmit();
        if (!ProjectStatusEnum.PROPOSED.equals(project.getStatus())) {
            technicalAssistanceSubmit.setRemainingAwardHours(technicalAssistanceSubmit.getRemainingAwardHours() + project.getEstimatedHoursNeeded());
            technicalAssistanceSubmitRepository.save(technicalAssistanceSubmit);
        }
        project.setStatus(ProjectStatusEnum.DENIED);
        project.setDeniedDate(Instant.now());
        project.setRemainingAwardHours(technicalAssistanceSubmit.getRemainingAwardHours() - project.getEstimatedHoursNeeded());
        projectRepository.save(project);

        // Technical Advisor receives an email
        TechnicalAdvisor technicalAdvisor = project.getTechnicalAdvisor();
        BusinessOwner businessOwner = project.getBusinessOwner();
        if (Objects.nonNull(technicalAdvisor) && Objects.nonNull(businessOwner)) {
            User technicalAdvisorUser = technicalAdvisor.getUser();
            userRepository
                .findCommunityPartnerNavigatorById(project.getVendor().getId())
                .ifPresent(navigatorUser -> {
                    inviteService.sendDenyProject(
                        technicalAdvisorUser.getEmail(),
                        businessOwner.getUser().getNormalizedFullName(),
                        technicalAdvisorUser.getFirstName(),
                        navigatorUser.getNormalizedFullName(),
                        navigatorUser.getEmail(),
                        project.getPortal()
                    );
                });
        }
    }

    private void updateApprovedProjectFields(Project project, boolean isBusinessOwner) {
        if (isBusinessOwner) {
            project.setWorkInProgressDate(Instant.now());
            project.setStatus(ProjectStatusEnum.WORK_IN_PROGRESS);
        } else {
            project.setStatus(ProjectStatusEnum.VENDOR_APPROVED);
            TechnicalAssistanceSubmit technicalAssistanceSubmit = project.getTechnicalAssistanceSubmit();
            project.setRemainingAwardHours(technicalAssistanceSubmit.getRemainingAwardHours() - project.getEstimatedHoursNeeded());
        }
        projectRepository.save(project);
    }

    private void updateRemainingAwardHours(Project project) {
        TechnicalAssistanceSubmit technicalAssistanceSubmit = project.getTechnicalAssistanceSubmit();
        technicalAssistanceSubmit.setRemainingAwardHours(
            Objects.nonNull(project.getRemainingAwardHours())
                ? project.getRemainingAwardHours()
                : technicalAssistanceSubmit.getRemainingAwardHours() - project.getEstimatedHoursNeeded()
        );
        technicalAssistanceSubmitRepository.save(technicalAssistanceSubmit);
    }

    /**
     * @param user User
     * @return businessName String
     */
    private String getBusinessName(User user) {
        String originalRole = getUserOriginalRole(user);
        return getBusinessNameIfApplicable(user, originalRole);
    }

    private String getUserOriginalRole(User user) {
        return user
            .getAuthorities()
            .stream()
            .findFirst()
            .map(Authority::getName)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Role")));
    }

    /**
     * @param user         User
     * @param originalRole String
     * @return businessNameIfApplicable String
     */
    private String getBusinessNameIfApplicable(User user, String originalRole) {
        if (RoleEnum.ROLE_BUSINESS_OWNER.getValue().equals(originalRole)) {
            var answerBusinessInfoMap = userFormService.getAnswerUserByQuestionCode(
                user.getId(),
                List.of(FormConstant.PORTAL_INTAKE_QUESTION_BUSINESS)
            );
            return Optional.ofNullable(answerBusinessInfoMap.get(FormConstant.PORTAL_INTAKE_QUESTION_BUSINESS)).orElse(StringUtils.EMPTY);
        }
        return StringUtils.EMPTY;
    }

    /**
     * Save attachments
     *
     * @param attachmentsRequest List<RequestAttachmentFile>
     * @param projectId          UUID
     */
    private void saveAttachments(List<RequestAttachmentFile> attachmentsRequest, UUID projectId) {
        if (attachmentsRequest != null && !attachmentsRequest.isEmpty()) {
            var attachmentEntities = attachmentsRequest.stream().map(file -> entityAttachmentMapper.toEntity(file, projectId)).toList();

            if (!attachmentEntities.isEmpty()) {
                entityAttachmentRepository.saveAll(attachmentEntities);
            }
        }
    }

    /**
     * Validate and get required entities
     *
     * @param request RequestCreateProject
     * @return ProjectCreationContext
     */
    private ProjectCreationContext validateAndGetEntities(RequestCreateProject request) {
        // 1. Validate Portal
        Portal portal = projectHelper.findEntityById(portalRepository, request.getPortalId(), VALIDATE_PORTAL);

        // 2. Validate Business Owner and Application
        TechnicalAssistanceSubmit technicalAssistanceSubmit = projectHelper.getApplicationForProject(
            portal.getId(),
            request.getApplicationId()
        );

        User businessOwnerUser = technicalAssistanceSubmit.getUser();
        BusinessOwner businessOwner = businessOwnerRepository
            .findByUserId(businessOwnerUser.getId())
            .orElseThrow(() -> new NotFoundException(MessageHelper.getMessage(Message.Keys.E0010, VALIDATE_BUSINESS_OWNER)));

        // 3. Validate Project Name
        projectHelper.validateProjectNameUniqueness(request.getProjectName());

        // 4. Validate Vendor
        CommunityPartner vendor = projectHelper.findEntityByIdAndPortalId(
            communityPartnerRepository::findVendorByIdAndPortalId,
            request.getVendorId(),
            request.getPortalId(),
            "Vendor"
        );

        // 5. Validate Category
        Category category = projectHelper.findEntityById(categoryRepository, request.getCategoryId(), VALIDATE_CATEGORY);

        // 6. Validate Technical Advisor
        TechnicalAdvisor assignAdvisor = projectHelper.findEntityByIdAndCommunityPartnerId(
            technicalAdvisorRepository::findByIdAndCommunityPartnerId,
            request.getAssignAdvisorId(),
            request.getVendorId(),
            "Assign Advisor"
        );

        validateProjectDetails(request, technicalAssistanceSubmit.getRemainingAwardHours());

        return new ProjectCreationContext(portal, businessOwner, vendor, category, assignAdvisor, technicalAssistanceSubmit);
    }

    private ProjectCreationContext validateAndGetEntitiesForAdditionalScopeOfWork(RequestCreateProject request) {
        var appointment = appointmentRepository
            .findById(request.getAppointmentId())
            .orElseThrow(() -> new NotFoundException(MessageHelper.getMessage(Message.Keys.E0010, VALIDATE_APPOINTMENT)));

        var technicalAssistanceSubmit = appointment.getTechnicalAssistanceSubmit();

        if (Objects.isNull(technicalAssistanceSubmit)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, VALIDATE_APPLICATION));
        }

        var portal = technicalAssistanceSubmit.getPortal();

        if (Objects.isNull(portal)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, VALIDATE_PORTAL));
        }

        var vendor = appointment.getCommunityPartner();

        if (technicalAssistanceSubmit.getUser() == null) {
            throw new NotFoundException(MessageHelper.getMessage(Message.Keys.E0010, VALIDATE_BUSINESS_OWNER_USER));
        }

        BusinessOwner businessOwner = businessOwnerRepository
            .findByUserId(technicalAssistanceSubmit.getUser().getId())
            .orElseThrow(() -> new NotFoundException(MessageHelper.getMessage(Message.Keys.E0010, VALIDATE_BUSINESS_OWNER)));

        var assignAdvisor = appointment.getTechnicalAdvisor();

        projectHelper.validateProjectNameUniqueness(request.getProjectName());

        var category = projectHelper.findEntityById(categoryRepository, request.getCategoryId(), VALIDATE_CATEGORY);

        validateProjectDetails(request, technicalAssistanceSubmit.getRemainingAwardHours());

        return new ProjectCreationContext(portal, businessOwner, vendor, category, assignAdvisor, technicalAssistanceSubmit);
    }

    private void validateProjectDetails(RequestCreateProject request, Float remainingAwardHours) {
        projectHelper.validateCompletionDate(request.getProposedStartDate(), request.getEstimatedCompletionDate());
        projectHelper.validateEstimatedHoursNeeded(request.getEstimatedHoursNeeded());
        projectHelper.validateEstimatedHoursNeeded(request.getEstimatedHoursNeeded(), remainingAwardHours);
        projectHelper.validateMaxFiles(request.getAttachments().size(), MAX_SECTION_FILES, VALIDATE_NUM_OF_FILES);
    }

    /**
     * Build new project entity
     *
     * @param request RequestCreateProject
     * @param context ProjectCreationContext
     * @return Project
     */
    private Project buildNewProject(RequestCreateProject request, ProjectCreationContext context) {
        // Build project entity with all required properties
        Project project = projectMapper.toEntity(request);

        // Set relationships
        project.setPortal(context.getPortal());
        project.setBusinessOwner(context.getBusinessOwner());
        project.setVendor(context.getVendor());
        project.setTechnicalAdvisor(context.getAssignAdvisor());
        project.setCategoryId(context.getCategory().getId());
        project.setTechnicalAssistanceSubmit(context.getTechnicalAssistanceSubmit());

        // Set status and dates
        project.setStatus(ProjectStatusEnum.PROPOSED);
        project.setProposedStartDate(request.getProposedStartDate());
        project.setEstimatedCompletionDate(request.getEstimatedCompletionDate());

        // Set other properties
        project.setProjectName(request.getProjectName());
        project.setScopeOfWork(request.getScopeOfWork());
        project.setEstimatedHoursNeeded(request.getEstimatedHoursNeeded());

        project.setAppointmentId(request.getAppointmentId());
        project.setIsAdditionalWork(Objects.nonNull(request.getAppointmentId()));

        return project;
    }

    /**
     * Validate and get required entities
     *
     * @param request RequestUpdateProject
     */
    private void validateUpdateRequest(RequestUpdateProject request, Project project) {
        if (!request.getProjectName().equals(project.getProjectName())) {
            projectHelper.validateProjectNameUniqueness(request.getProjectName());
        }

        projectHelper.validateCompletionDate(request.getProposedStartDate(), request.getEstimatedCompletionDate());
        projectHelper.validateEstimatedHoursNeeded(request.getEstimatedHoursNeeded());
        projectHelper.validateEstimatedHoursNeeded(
            request.getEstimatedHoursNeeded(),
            project.getTechnicalAssistanceSubmit().getRemainingAwardHours()
        );
        projectHelper.validateMaxFiles(request.getAttachments().size(), MAX_SECTION_FILES, "Num of Files");
    }

    /**
     * Update project fields
     *
     * @param project Project
     * @param request RequestUpdateProject
     */
    private void updateProjectFields(Project project, RequestUpdateProject request) {
        // Validate and get related entities
        Category category = projectHelper.findEntityById(categoryRepository, request.getCategoryId(), "Category");
        ServiceOffered serviceOffered = projectHelper.findEntityById(serviceOfferedRepository, request.getServiceId(), "ServiceOffered");
        TechnicalAssistanceSubmit technicalAssistanceSubmit = project.getTechnicalAssistanceSubmit();

        // Update basic information
        project.setProjectName(request.getProjectName());
        project.setScopeOfWork(request.getScopeOfWork());
        project.setRemainingAwardHours(
            Objects.nonNull(project.getRemainingAwardHours())
                ? technicalAssistanceSubmit.getRemainingAwardHours() + project.getEstimatedHoursNeeded() - request.getEstimatedHoursNeeded()
                : null
        );
        project.setEstimatedHoursNeeded(request.getEstimatedHoursNeeded());

        // Update dates
        project.setProposedStartDate(request.getProposedStartDate());
        project.setEstimatedCompletionDate(request.getEstimatedCompletionDate());

        // Update relationships
        project.setCategoryId(category.getId());
        project.setServiceId(serviceOffered.getId());
        var preStatus = project.getStatus();
        // Update status
        project.setStatus(ProjectStatusEnum.valueOf(request.getStatus()));
        if (!project.getStatus().equals(preStatus) && ProjectStatusEnum.COMPLETED.equals(project.getStatus())) {
            project.setCompletedDate(Instant.now());
        }
    }

    /**
     * Set remaining award hours
     *
     * @param project  Project
     * @param response ResponseProjectHeader
     */
    private void setRemainingAwardHours(Project project, ResponseProjectHeader response) {
        if (Objects.nonNull(project.getTechnicalAssistanceSubmit())) {
            TechnicalAssistanceSubmit technicalAssistanceSubmit = project.getTechnicalAssistanceSubmit();
            response.setRemainingAwardHours(
                Objects.nonNull(project.getRemainingAwardHours())
                    ? project.getRemainingAwardHours()
                    : technicalAssistanceSubmit.getRemainingAwardHours() - project.getEstimatedHoursNeeded()
            );
        }
    }

    /**
     * Handle update project attachments
     *
     * @param projectId       UUID
     * @param attachmentFiles List<RequestAttachmentFile>
     * @param oldFiles        Set<ProjectAttachment>
     * @return List<ProjectAttachment>
     */
    private List<EntityAttachment> handleUpdateProjectAttachment(
        UUID projectId,
        List<RequestAttachmentFile> attachmentFiles,
        List<EntityAttachment> oldFiles
    ) {
        // If there are no new attachments, delete all old attachments
        if (CollectionUtils.isEmpty(attachmentFiles)) {
            if (!CollectionUtils.isEmpty(oldFiles)) {
                entityAttachmentRepository.deleteAll(oldFiles);
            }
            return Collections.emptyList();
        }

        // Classify files

        // 1. Handle current files (already have ID)
        List<EntityAttachment> currentFiles = attachmentFiles
            .stream()
            .filter(f -> Objects.nonNull(f.getId()))
            .map(f -> entityAttachmentMapper.toEntity(f, projectId))
            .toList();
        List<EntityAttachment> result = new ArrayList<>(currentFiles);

        // 2. Determine and remove files to remove
        List<EntityAttachment> filesToRemove = oldFiles
            .stream()
            .filter(
                oldFile ->
                    attachmentFiles
                        .stream()
                        .filter(f -> Objects.nonNull(f.getId()))
                        .noneMatch(newFile -> Objects.equals(UUIDUtils.convertToUUID(newFile.getId()), oldFile.getId()))
            )
            .toList();
        if (!filesToRemove.isEmpty()) {
            entityAttachmentRepository.deleteAll(filesToRemove);
        }

        // 3. Handle new files (no ID yet)
        List<EntityAttachment> newFiles = attachmentFiles
            .stream()
            .filter(f -> Objects.isNull(f.getId()))
            .map(f -> entityAttachmentMapper.toEntity(f, projectId))
            .toList();
        if (!newFiles.isEmpty()) {
            var savedNewFiles = entityAttachmentRepository.saveAll(newFiles);
            result.addAll(savedNewFiles);
        }

        return result;
    }

    public boolean existsByProjectId(UUID projectId) {
        return projectRepository.existsById(projectId);
    }

    public boolean checkExistingProjectWithBusinessOwnerId(UUID projectId) {
        var currentUser = SecurityUtils.getCurrentUser(userRepository);
        if (Objects.isNull(currentUser)) {
            throw new AccessDeniedException("Role Business Owner");
        }
        var businessOwner = businessOwnerRepository.findByUserId(currentUser.getId())
            .orElseThrow(() -> new AccessDeniedException("Role Business Owner"));
         return projectRepository.existsByIdAndBusinessOwner(projectId, businessOwner);
    }
}
