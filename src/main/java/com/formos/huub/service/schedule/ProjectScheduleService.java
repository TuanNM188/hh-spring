package com.formos.huub.service.schedule;

import com.formos.huub.domain.constant.EmailTemplatePathsConstants;
import com.formos.huub.domain.entity.Project;
import com.formos.huub.domain.entity.ProjectUpdate;
import com.formos.huub.domain.enums.ProjectStatusEnum;
import com.formos.huub.repository.ProjectRepository;
import com.formos.huub.repository.ProjectUpdateRepository;
import com.formos.huub.service.project.ProjectService;
import com.formos.huub.service.pushnotification.PushNotificationService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProjectScheduleService {

    private static final String PROJECT_UPDATE_DESCRIPTION = "Auto-denied due to inactivity before the proposed start date.";

    @Value("${schedule.timezone}")
    private String timeZoneId;

    private ZoneId configuredZone;

    @PostConstruct
    public void init() {
        this.configuredZone = ZoneId.of(timeZoneId);
    }

    private final ProjectService projectService;
    private final ProjectRepository projectRepository;
    private final ProjectUpdateRepository projectUpdateRepository;

    private final PushNotificationService pushNotificationService;

    // A report summarizing the completion of the project HAS NOT been submitted by the advisor. Current Date > project estimated completion date
    public void jobScheduleAutoOverdueProject() {

        ZonedDateTime pstNow = ZonedDateTime.now(configuredZone);
        Instant pstNowStart = pstNow.toLocalDate().atStartOfDay(configuredZone).toInstant();

        try {
            log.info("Starting auto change Project status to Overdue process");
            int updatedCount = projectRepository.updateStatusProjectByStatusAndNoReportSubmitted(ProjectStatusEnum.OVERDUE
                                                                ,ProjectStatusEnum.WORK_IN_PROGRESS
                                                                ,pstNowStart);
            log.info("Auto change project status to Overdue completed. Updated {} projects.", updatedCount);
        } catch (Exception e) {
            log.error("Error occurred during schedule auto change project status to Overdue when A report summarizing the completion of the project HAS NOT been submitted by the advisor. Current Date > project estimated completion date: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to execute schedule auto change project status to Overdue when A report summarizing the completion of the project HAS NOT been submitted by the advisor. Current Date > project estimated completion date", e);
        }
    }

    /**
     * The Navigator will review proposed projects, approve or deny them. If the project is not approved within 3 days, send a reminder email to the Navigator.
     */
    public void jobScheduleReminderNotApprovedProject() {
        ZonedDateTime pstNow = ZonedDateTime.now(configuredZone);
        Instant threeDaysAgoStart = pstNow.minusDays(3).toLocalDate().atStartOfDay(configuredZone).toInstant();
        Instant threeDaysAgoEnd = pstNow.minusDays(2).toLocalDate().atStartOfDay(configuredZone).toInstant();
        List<Project> projects = projectRepository.findProjectIdByStatus(ProjectStatusEnum.PROPOSED, threeDaysAgoStart, threeDaysAgoEnd);
        try {
            projects.forEach(project -> {
                log.info("Send mail reminder to the {} project is not approved within 3 days", project.getProjectName());
                pushNotificationService.buildTemplateReminderProject(
                    project,
                    EmailTemplatePathsConstants.REMINDER_NOT_APPROVED_PROJECT,
                    "email.reminder.project.notApproved.title"
                );
            });
        } catch (Exception e) {
            log.error("Error occurred during schedule Send mail reminder to the {} project is not approved within 3 days process", e.getMessage(), e);
            throw new RuntimeException("Failed to execute schedule reminder project is not approved within 3 days job", e);
        }
    }

    /**
     * Update Projects with a PROPOSED status that are not approved by the Proposed Start Date
     */
    public void jobScheduleAutoDenialProject() {
        ZonedDateTime pstNow = ZonedDateTime.now(configuredZone);
        Instant pstNowEnd = pstNow.plusDays(1).toLocalDate().atStartOfDay(configuredZone).toInstant();
        var listStatus = Arrays.asList(ProjectStatusEnum.PROPOSED, ProjectStatusEnum.VENDOR_APPROVED);
        List<UUID> projectIds = projectRepository.findProjectIdByStatusAndBeforeProposedStartDate(listStatus, pstNowEnd);
        try {
            log.info("Starting auto-denial process...");
            projectIds.forEach(projectService::denyProject);
            addProjectUpdate(projectIds);
            log.info("Auto-denial completed. Updated {} projects.", projectIds.size());
        } catch (Exception e) {
            log.error("Error occurred during auto-denial process: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to execute auto-denial job", e);
        }
    }

    /**
     *
     * @param projectIds List<UUID>
     */
    private void addProjectUpdate(List<UUID> projectIds) {
        if (!CollectionUtils.isEmpty(projectIds)) {
            List<ProjectUpdate> projectUpdates = new ArrayList<>();
            projectIds
                .forEach(id -> {
                    Project project = new Project();
                    project.setId(id);

                    ProjectUpdate projectUpdate = new ProjectUpdate();
                    projectUpdate.setProject(project);
                    projectUpdate.setDescription(PROJECT_UPDATE_DESCRIPTION);

                    projectUpdates.add(projectUpdate);
                });

            projectUpdateRepository.saveAll(projectUpdates);
        }
    }

}
