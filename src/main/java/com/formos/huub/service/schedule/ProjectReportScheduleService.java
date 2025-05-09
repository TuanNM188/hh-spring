/**
 * ***************************************************
 * * Description :
 * * File        : ProjectReportScheduleService
 * * Author      : Hung Tran
 * * Date        : Feb 24, 2025
 * ***************************************************
 **/
package com.formos.huub.service.schedule;

import com.formos.huub.domain.constant.EmailTemplatePathsConstants;
import com.formos.huub.domain.entity.Project;
import com.formos.huub.repository.ProjectRepository;
import com.formos.huub.service.pushnotification.PushNotificationService;
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProjectReportScheduleService {

    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();

    @Value("${schedule.timezone}")
    private String timeZoneId;

    private ZoneId configuredZone;

    @PostConstruct
    public void init() {
        this.configuredZone = ZoneId.of(timeZoneId);
    }

    private final ProjectRepository projectRepository;
    private final PushNotificationService pushNotificationService;

    public void sendMailReminder() {
        ZonedDateTime pstNow = ZonedDateTime.now(configuredZone);

        // Calculate the time range for each reminder milestone

        // Calculate the time range for 1 week before
        Instant oneWeekBeforeStart = pstNow.plusDays(7).toLocalDate().atStartOfDay(configuredZone).toInstant();
        Instant oneWeekBeforeEnd = pstNow.plusDays(7).toLocalDate().plusDays(1).atStartOfDay(configuredZone).toInstant();

        log.info("oneWeekBeforeStart: {}", oneWeekBeforeStart);
        log.info("oneWeekBeforeEnd: {}", oneWeekBeforeEnd);

        // Calculate the time range for 1 day before
        Instant oneDayBeforeStart = pstNow.plusDays(1).toLocalDate().atStartOfDay(configuredZone).toInstant();
        Instant oneDayBeforeEnd = pstNow.plusDays(1).toLocalDate().plusDays(1).atStartOfDay(configuredZone).toInstant();

        log.info("oneDayBeforeStart: {}", oneDayBeforeStart);
        log.info("oneDayBeforeEnd: {}", oneDayBeforeEnd);

        // Calculate the time range for 1 day after
        Instant oneDayAfterStart = pstNow.minusDays(1).toLocalDate().atStartOfDay(configuredZone).toInstant();
        Instant oneDayAfterEnd = pstNow.minusDays(1).toLocalDate().plusDays(1).atStartOfDay(configuredZone).toInstant();

        log.info("oneDayAfterStart: {}", oneDayAfterStart);
        log.info("oneDayAfterEnd: {}", oneDayAfterEnd);

        // Calculate the time range for 7 days after
        Instant sevenDaysAfterStart = pstNow.minusDays(7).toLocalDate().atStartOfDay(configuredZone).toInstant();
        Instant sevenDaysAfterEnd = pstNow.minusDays(7).toLocalDate().plusDays(1).atStartOfDay(configuredZone).toInstant();

        log.info("sevenDaysAfterStart: {}", sevenDaysAfterStart);
        log.info("sevenDaysAfterEnd: {}", sevenDaysAfterEnd);

        // Calculate the time range for 14 days after
        Instant fourteenDaysAfterStart = pstNow.minusDays(14).toLocalDate().atStartOfDay(configuredZone).toInstant();
        Instant fourteenDaysAfterEnd = pstNow.minusDays(14).toLocalDate().plusDays(1).atStartOfDay(configuredZone).toInstant();

        log.info("fourteenDaysAfterStart: {}", fourteenDaysAfterStart);
        log.info("fourteenDaysAfterEnd: {}", fourteenDaysAfterEnd);

        // Calculate the time range for 30 days after
        Instant thirtyDaysAfterStart = pstNow.minusDays(30).toLocalDate().atStartOfDay(configuredZone).toInstant();
        Instant thirtyDaysAfterEnd = pstNow.minusDays(30).toLocalDate().plusDays(1).atStartOfDay(configuredZone).toInstant();

        log.info("thirtyDaysAfterStart: {}", thirtyDaysAfterStart);
        log.info("thirtyDaysAfterEnd: {}", thirtyDaysAfterEnd);

        // Send reminder 1 week before the estimated completion date
        List<Project> projectsDueInOneWeek = projectRepository.findByEstimatedCompletionDateBetween(oneWeekBeforeStart, oneWeekBeforeEnd);
        projectsDueInOneWeek.forEach(project -> {
            log.info("Send mail reminder to project manager for project {} due in 1 week", project.getProjectName());
            pushNotificationService.buildTemplateReminderProjectReport(
                project,
                EmailTemplatePathsConstants.ONE_WEEK_BEFORE_ESTIMATED_COMPLETION_DATE,
                "email.reminder.project.oneWeek.before.completionDate.title"
            );
        });

        // Send reminder 1 day before the estimated completion date
        List<Project> projectsDueInOneDay = projectRepository.findByEstimatedCompletionDateBetween(oneDayBeforeStart, oneDayBeforeEnd);
        projectsDueInOneDay.forEach(project -> {
            log.info("Send mail reminder to project manager for project {} due in 1 day", project.getProjectName());
            pushNotificationService.buildTemplateReminderProjectReport(
                project,
                EmailTemplatePathsConstants.ONE_DAY_BEFORE_ESTIMATED_COMPLETION_DATE,
                "email.reminder.project.oneDay.before.completionDate.title"
            );
        });

        // Send reminder 1 day after the estimated completion date
        List<Project> projectsOverdueByOneDay = projectRepository.findByEstimatedCompletionDateBetween(oneDayAfterStart, oneDayAfterEnd);
        projectsOverdueByOneDay.forEach(project -> {
            log.info("Send mail reminder to project manager for project {} overdue by 1 day", project.getProjectName());
            pushNotificationService.buildTemplateReminderProjectReport(
                project,
                EmailTemplatePathsConstants.ONE_DAY_AFTER_ESTIMATED_COMPLETION_DATE,
                "email.reminder.project.oneDay.after.completionDate.title"
            );
        });

        // Send reminder 7 days after the estimated completion date
        List<Project> projectsOverdueBySevenDays = projectRepository.findByEstimatedCompletionDateBetween(
            sevenDaysAfterStart,
            sevenDaysAfterEnd
        );
        projectsOverdueBySevenDays.forEach(project -> {
            log.info("Send mail reminder to project manager for project {} overdue by 1 week", project.getProjectName());
            pushNotificationService.buildTemplateReminderProjectReport(
                project,
                EmailTemplatePathsConstants.ONE_WEEK_AFTER_ESTIMATED_COMPLETION_DATE,
                "email.reminder.project.oneWeek.after.completionDate.title"
            );
        });

        // Send reminder 14 days after the estimated completion date
        List<Project> projectsOverdueByFourteenDays = projectRepository.findByEstimatedCompletionDateBetween(
            fourteenDaysAfterStart,
            fourteenDaysAfterEnd
        );
        projectsOverdueByFourteenDays.forEach(project -> {
            log.info("Send mail reminder to project manager for project {} overdue by 2 weeks", project.getProjectName());
            pushNotificationService.buildTemplateReminderProjectReport(
                project,
                EmailTemplatePathsConstants.FOURTEEN_DAYS_AFTER_ESTIMATED_COMPLETION_DATE,
                "email.reminder.project.14Days.after.completionDate.title"
            );
        });

        // Send reminder 30 days after the estimated completion date
        List<Project> projectsOverdueByThirtyDays = projectRepository.findByEstimatedCompletionDateBetween(
            thirtyDaysAfterStart,
            thirtyDaysAfterEnd
        );
        projectsOverdueByThirtyDays.forEach(project -> {
            log.info("Send mail reminder to project manager for project {} overdue by 1 month", project.getProjectName());
            pushNotificationService.buildTemplateReminderProjectReport(
                project,
                EmailTemplatePathsConstants.THIRTY_DAYS_AFTER_ESTIMATED_COMPLETION_DATE,
                "email.reminder.project.30Days.after.completionDate.title"
            );
        });
    }
}
