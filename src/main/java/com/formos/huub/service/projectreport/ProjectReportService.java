package com.formos.huub.service.projectreport;

import com.formos.huub.domain.constant.ActiveCampaignConstant;
import com.formos.huub.domain.entity.*;
import com.formos.huub.domain.enums.EntityTypeEnum;
import com.formos.huub.domain.enums.ProjectStatusEnum;
import com.formos.huub.domain.request.common.RequestAttachmentFile;
import com.formos.huub.domain.request.projectreport.RequestCreateProjectReport;
import com.formos.huub.domain.request.projectreport.RequestUpdateProjectReport;
import com.formos.huub.domain.response.project.ResponseProjectReport;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.utils.StringUtils;
import com.formos.huub.framework.utils.UUIDUtils;
import com.formos.huub.helper.member.MemberHelper;
import com.formos.huub.helper.project.ProjectHelper;
import com.formos.huub.mapper.entityattachment.EntityAttachmentMapper;
import com.formos.huub.mapper.projectreport.ProjectReportMapper;
import com.formos.huub.repository.EntityAttachmentRepository;
import com.formos.huub.repository.ProjectReportRepository;
import com.formos.huub.repository.ProjectRepository;
import com.formos.huub.repository.TechnicalAssistanceSubmitRepository;
import com.formos.huub.service.activecampaign.ActiveCampaignStrategy;
import com.formos.huub.service.common.SequenceService;
import com.formos.huub.service.pdf.PdfService;
import com.formos.huub.service.pushnotification.PushNotificationService;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectReportService {

    ProjectReportRepository projectReportRepository;

    EntityAttachmentRepository entityAttachmentRepository;

    TechnicalAssistanceSubmitRepository technicalAssistanceSubmitRepository;

    ProjectReportMapper projectReportMapper;

    EntityAttachmentMapper entityAttachmentMapper;

    ProjectRepository projectRepository;

    ProjectHelper projectHelper;

    PdfService pdfService;

    PushNotificationService pushNotificationService;
    SequenceService sequenceGeneratorService;

    ActiveCampaignStrategy activeCampaignStrategy;

    MemberHelper memberHelper;

    private static final Integer MAX_SECTION_FILES = 8;
    private static final String PROJECT_REPORT_SEQUENCE = "project_report_sequence";

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
     * create Project Report
     *
     * @param request RequestCreateProjectReport
     */
    public UUID createProjectReport(@Valid RequestCreateProjectReport request) {
        var projectId = UUIDUtils.toUUID(request.getProjectId());
        var project = getProject(projectId);

        //Validate Attachments
        projectHelper.validateMaxFiles(request.getAttachments().size(), MAX_SECTION_FILES, "Num of Files");
        // Validate Require Service Outcomes
        projectHelper.validateRequireServiceOutcomes(request.getServiceOutcomes().size(), "Service Outcomes");
        //Must not exceed the project's estimated hours.
        projectHelper.validateHoursCompleted(request.getHoursCompleted(), project.getEstimatedHoursNeeded());
        // Validate Confirmation is Checked
        projectHelper.validateConfirmation(request.getConfirmation(), "Confirmation");

        // update project status
        project = updateProjectFields(project, ProjectStatusEnum.COMPLETED);
        var projectReport = projectReportMapper.toEntity(request, projectId);
        Long reportNumber = sequenceGeneratorService.getNextSequenceValue(PROJECT_REPORT_SEQUENCE);
        projectReport.setReportNumber(reportNumber.toString());
        projectReport = pdfService.generatePdfProjectReport(project, projectReport);
        var newProjectReport = projectReportRepository.save(projectReport);

        // Update Hours Available/Remaining (HUUBPlatformRebuild-Q156)
        updateRemainingHours(project, newProjectReport);

        // Send Email Post Project Feedback Form
        pushNotificationService.buildTemplateProjectReport(project, "email.invitation.projectReport.title");

        if (CollectionUtils.isEmpty(request.getAttachments())) {
            return newProjectReport.getId();
        }

        //Save attachments
        saveAttachments(request.getAttachments(), newProjectReport.getId());

        return newProjectReport.getId();
    }

    // HUUBPlatformRebuild-Q156
    private void updateRemainingHours(Project project, ProjectReport projectReport) {
        TechnicalAssistanceSubmit technicalAssistanceSubmit = project.getTechnicalAssistanceSubmit();
        float remainingHours =
            technicalAssistanceSubmit.getRemainingAwardHours() - (projectReport.getHoursCompleted() - project.getEstimatedHoursNeeded());
        technicalAssistanceSubmit.setRemainingAwardHours(remainingHours);
        technicalAssistanceSubmitRepository.save(technicalAssistanceSubmit);

        boolean applicationCompleted = BigDecimal.valueOf(remainingHours).compareTo(BigDecimal.ZERO) == 0;
        if (applicationCompleted) {
            syncCompleteProjectToActiveCampaign(technicalAssistanceSubmit.getUser().getId());
        }
    }

    /**
     * Sync Complete Project To ActiveCampaign: FIELD_TA_STATUS_V2
     *
     * @param userId UUID
     */
    private void syncCompleteProjectToActiveCampaign(UUID userId) {
        User user = memberHelper.getUserById(userId);
        if (Objects.isNull(user)) {
            return;
        }
        Map<String, String> campaignValueMap = new HashMap<>();
        campaignValueMap.put(ActiveCampaignConstant.FIELD_TA_STATUS_V2, ActiveCampaignConstant.COMPLETED);
        activeCampaignStrategy.syncValueActiveCampaignApplication(user, campaignValueMap);
    }

    /**
     * update Project Report
     *
     * @param request RequestUpdateProjectReport
     */
    public ResponseProjectReport updateProjectReport(UUID id, @Valid RequestUpdateProjectReport request) {
        var projectReport = projectReportRepository
            .findById(id)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Project Report")));
        var projectId = UUIDUtils.toUUID(request.getProjectId());
        var project = getProject(projectId);
        //Validate Attachments
        projectHelper.validateMaxFiles(request.getAttachments().size(), MAX_SECTION_FILES, "Num of Files");

        // Validate Require Service Outcomes
        projectHelper.validateRequireServiceOutcomes(request.getServiceOutcomes().size(), "Service Outcomes");
        //Must not exceed the project's estimated hours.
        projectHelper.validateHoursCompleted(request.getHoursCompleted(), project.getEstimatedHoursNeeded());
        // Validate Confirmation is Checked
        projectHelper.validateConfirmation(request.getConfirmation(), "Confirmation");
        // update project status
        project = updateProjectFields(project, ProjectStatusEnum.COMPLETED);
        // update project report
        updateProjectReportFields(projectReport, request);

        projectReport = pdfService.generatePdfProjectReport(project, projectReport);
        var updatedProjectReport = projectReportRepository.save(projectReport);
        var attachments = entityAttachmentRepository.findByEntityIdAndEntityType(
            updatedProjectReport.getId(),
            EntityTypeEnum.PROJECT_REPORT
        );

        List<EntityAttachment> projectReportAttachments = handleUpdateProjectReportAttachment(
            projectReport.getId(),
            request.getAttachments(),
            attachments
        )
            .stream()
            .map(entityAttachmentMapper::toResponse)
            .toList();

        // Build response
        ResponseProjectReport response = projectReportMapper.toResponse(updatedProjectReport);

        response.setAttachments(projectReportAttachments);
        return response;
    }

    /**
     * @param projectId UUID
     * @return ResponseProjectReport
     */
    public ResponseProjectReport getProjectReport(UUID projectId) {
        var projectReport = this.projectReportRepository.findByProjectId(projectId).orElse(null);
        if (Objects.isNull(projectReport)) {
            return null;
        }
        var attachments = entityAttachmentRepository.findByEntityIdAndEntityType(projectReport.getId(), EntityTypeEnum.PROJECT_REPORT);
        var response = projectReportMapper.toResponse(projectReport);
        response.setAttachments(attachments);
        return response;
    }

    /**
     * @param projectReport ProjectReport
     * @param request       RequestUpdateProjectReport
     */
    private void updateProjectReportFields(ProjectReport projectReport, RequestUpdateProjectReport request) {
        projectReport.setHoursCompleted(request.getHoursCompleted());
        projectReport.setDescription(request.getDescription());
        projectReport.setServiceOutcomes(StringUtils.convertListToString(request.getServiceOutcomes()));
        projectReport.setFeedback(request.getFeedback());
    }

    /**
     * @param project
     * @param status
     */
    private Project updateProjectFields(Project project, ProjectStatusEnum status) {
        project.setStatus(status);
        if (ProjectStatusEnum.COMPLETED.equals(project.getStatus())) {
            project.setCompletedDate(Instant.now());
        }
        return projectRepository.save(project);
    }

    /**
     * @param attachments     List<RequestPRAttachmentFile>
     * @param projectReportId projectReportId
     */
    private void saveAttachments(List<RequestAttachmentFile> attachments, UUID projectReportId) {
        if (!CollectionUtils.isEmpty(attachments)) {
            var attachmentEntities = attachments.stream().map(f -> entityAttachmentMapper.toEntity(f, projectReportId)).toList();
            entityAttachmentRepository.saveAll(attachmentEntities);
        }
    }

    /**
     * @param projectReportId UUID
     * @param attachmentFiles List<RequestPRAttachmentFile>
     * @param oldFiles        Set<ProjectReportAttachment>
     * @return List<ProjectReportAttachment>
     */
    private List<EntityAttachment> handleUpdateProjectReportAttachment(
        UUID projectReportId,
        List<RequestAttachmentFile> attachmentFiles,
        List<EntityAttachment> oldFiles
    ) {
        if (CollectionUtils.isEmpty(attachmentFiles)) {
            if (!CollectionUtils.isEmpty(oldFiles)) {
                entityAttachmentRepository.deleteAll(oldFiles);
            }
            return Collections.emptyList();
        }

        // Classify files
        List<EntityAttachment> result = new ArrayList<>();

        //Handle current files (already have ID)
        List<EntityAttachment> currentFiles = attachmentFiles
            .stream()
            .filter(f -> Objects.nonNull(f.getId()))
            .map(f -> entityAttachmentMapper.toEntity(f, projectReportId))
            .toList();
        result.addAll(currentFiles);

        // Determine and remove files to remove
        List<EntityAttachment> filesToRemove = oldFiles
            .stream()
            .filter(
                oldFile ->
                    attachmentFiles
                        .stream()
                        .filter(f -> Objects.nonNull(f.getId()))
                        .noneMatch(t -> UUIDUtils.convertToUUID(t.getId()).equals(oldFile.getId()))
            )
            .collect(Collectors.toList());
        if (!filesToRemove.isEmpty()) {
            entityAttachmentRepository.deleteAll(filesToRemove);
        }

        List<EntityAttachment> newFiles = attachmentFiles
            .stream()
            .filter(f -> Objects.isNull(f.getId()))
            .map(f -> entityAttachmentMapper.toEntity(f, projectReportId))
            .toList();
        if (!newFiles.isEmpty()) {
            List<EntityAttachment> newProjectAttachments = entityAttachmentRepository.saveAll(newFiles);
            result.addAll(newProjectAttachments);
        }

        return result;
    }
}
