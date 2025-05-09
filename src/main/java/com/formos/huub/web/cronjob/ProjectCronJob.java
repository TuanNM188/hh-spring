package com.formos.huub.web.cronjob;

import com.formos.huub.service.schedule.ProjectReportScheduleService;
import java.time.Instant;

import com.formos.huub.service.schedule.ProjectScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProjectCronJob {

    private final ProjectReportScheduleService projectReportScheduleService;
    private final ProjectScheduleService projectScheduleService;

    // auto-denial mechanism for projects that are not approved before the proposed start date
    // This cron expression schedules the task to run daily at 00:00 PST .
    @Scheduled(cron = "0 0 0 * * ?", zone = "America/Los_Angeles")
    public void jobScheduleAutoDenialProject() {
        log.info("Start auto-denial mechanism for projects at: {}", Instant.now());
        projectScheduleService.jobScheduleAutoDenialProject();
    }

    // The Navigator will review proposed projects, approve or deny them. If the project is not approved within 3 days, send a reminder email to the Navigator.
    // This cron expression schedules the task to run daily at 00:00 PST .
    @Scheduled(cron = "0 0 0 * * ?", zone = "America/Los_Angeles")
    public void jobScheduleReminderNotApprovedProject() {
        log.info("Start send reminder for the project is not approved within 3 days at: {}", Instant.now());
        projectScheduleService.jobScheduleReminderNotApprovedProject();
    }

    @Scheduled(cron = "0 0 0 * * ?", zone = "America/Los_Angeles")
    public void sendMailReminder() {
        log.info("Start send mail reminder for projects at: {}", Instant.now());
        projectReportScheduleService.sendMailReminder();
    }

    @Scheduled(cron = "0 0 0 * * ?", zone = "America/Los_Angeles")
    public void jobScheduleAutoOverdueProject() {
        log.info("Start Auto change project status to Overdue at: {}", Instant.now());
        projectScheduleService.jobScheduleAutoOverdueProject();
    }

}
