package com.formos.huub.web.cronjob;

import com.formos.huub.domain.enums.CalendarTypeEnum;
import com.formos.huub.domain.enums.IntegrateByEnum;
import com.formos.huub.service.bookingsetting.BookingSettingService;
import com.formos.huub.service.schedule.CalendarEventScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CalendarEventCronJob {

    private final BookingSettingService bookingSettingService;

    private final CalendarEventScheduleService calendarEventScheduleService;

    @Scheduled(cron = "0 0 * * * *")
    public void jobScheduleSyncEventFromBookingSetting() {
        log.info("Start sync events from booking setting at: {}", Instant.now());
        bookingSettingService.jobScheduleSyncEventFromCalendarIntegrate();
    }

    /**
     * job Schedule Sync Huub Event
     */
    @Scheduled(cron = "0 0 1 * * ?", zone = "America/Los_Angeles")
    public void jobScheduleSyncHuubEvent() {
        log.info("Start sync huub events at: {}", Instant.now());
        calendarEventScheduleService.syncHuubEvents();
    }

    /**
     * job Schedule Sync Huub Event Registration
     */
    @Scheduled(cron = "0 30 1 * * ?", zone = "America/Los_Angeles")
    public void jobScheduleSyncHuubEventRegistrations() {
        log.info("Start sync huub event registration at: {}", Instant.now());
        calendarEventScheduleService.syncHuubEventRegistrations();
    }

    /**
     * job Schedule Sync Event From Calendar Integrate
     */
    @Scheduled(cron = "0 0 * * * *")
    public void jobScheduleSyncEventFromCalendarIntegrate() {
        log.info("Start sync events from calendar integrate at: {}", Instant.now());
        calendarEventScheduleService.syncEvents(List.of(CalendarTypeEnum.ICALENDAR, CalendarTypeEnum.EVENTBRITE), IntegrateByEnum.PORTAL);
    }

    /**
     * job Schedule Sync Event From Calendar Integrate
     */
    @Scheduled(cron = "0 0 * * * *")
    public void jobScheduleSyncEventFromCommunityPartner() {
        log.info("Start sync events from community partner at: {}", Instant.now());
        calendarEventScheduleService.syncEvents(List.of(CalendarTypeEnum.ICALENDAR, CalendarTypeEnum.EVENTBRITE), IntegrateByEnum.COMMUNITY_PARTNER);
    }

    // Run daily at midnight
    @Scheduled(cron = "0 0 0 * * ?")
    public void scheduledDeleteOldLogSyncEvents() {
        calendarEventScheduleService.deleteOldLogSyncEvents();
    }

    // Run daily at midnight
    @Scheduled(cron = "0 0 0 * * ?")
    public void scheduledDeleteOldLogCronJobEntries() {
        calendarEventScheduleService.deleteOldLogCronJobEntries();
    }

}
