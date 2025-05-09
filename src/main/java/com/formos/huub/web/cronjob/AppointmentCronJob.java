package com.formos.huub.web.cronjob;

import com.formos.huub.service.schedule.AppointmentScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentCronJob {

    private final AppointmentScheduleService appointmentScheduleService;


    // Implement automated pre-appointment notifications to remind both the Business Owner and Technical Advisor of their upcoming scheduled appointment.
    // This cron will run every 30 minutes, specifically at 00 and 30 minutes past each hour.
    @Scheduled(cron = "0 0,30 * * * ?", zone = "America/Los_Angeles")
    public void jobScheduleAutoDenialProject() {
        log.info("Start automated pre-appointment notifications to remind both the Business Owner and Technical Advisor of their upcoming scheduled appointment: {}", Instant.now());
        appointmentScheduleService.jobScheduleReminderPreAppointment();

    }
}
