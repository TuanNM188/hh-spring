package com.formos.huub.service.schedule;

import com.formos.huub.domain.entity.CalendarIntegration;
import com.formos.huub.domain.enums.CalendarStatusEnum;
import com.formos.huub.domain.enums.CalendarTypeEnum;
import com.formos.huub.domain.enums.IntegrateByEnum;
import com.formos.huub.repository.CalendarIntegrationRepository;
import com.formos.huub.repository.LogCronJobEntriesRepository;
import com.formos.huub.repository.LogSyncEventRepository;
import com.formos.huub.service.calendarevent.CalendarEventService;
import com.formos.huub.service.eventregistration.EventRegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CalendarEventScheduleService {

    private final CalendarIntegrationRepository calendarIntegrationRepository;

    private final CalendarEventService calendarEventService;
    private final EventRegistrationService eventRegistrationService;

    private final LogSyncEventRepository logSyncEventRepository;
    private final LogCronJobEntriesRepository logCronJobEntriesRepository;

    @Value("${schedule.remove-event-in-seconds}")
    Long removeEventInSeconds;

    /**
     * Sync Events
     *
     * @param types       List<CalendarTypeEnum>
     * @param integrateBy IntegrateByEnum
     */
    public void syncEvents(List<CalendarTypeEnum> types, IntegrateByEnum integrateBy) {
        List<CalendarIntegration> calendarIntegrations = calendarIntegrationRepository.findAllByCalendarStatusAndCalendarTypeInAndPortal(CalendarStatusEnum.ACTIVE, types, integrateBy);

        if (calendarIntegrations.isEmpty()) {
            log.info("No Calendar Integration found.");
            return;
        }

        calendarIntegrations.forEach(calendar -> {
            try {
                calendarEventService.handleSaveEvent(calendar);
            } catch (Exception e) {
                log.error("Error save event from link: {}", calendar.getUrl(), e);
            }
        });
    }

    /**
     * sync Huub Events
     *
     */
    public void syncHuubEvents() {
        try {
            calendarEventService.handleSaveHuubEvent();
        } catch (Exception e) {
            log.error("Error save event for huub organization", e);
        }
    }

    /**
     * sync Huub Event Registration
     *
     */
    public void syncHuubEventRegistrations() {
        try {
            eventRegistrationService.handleSaveEventRegister();
        } catch (Exception e) {
            log.error("Error save event registration for huub organization", e);
        }
    }

    public void deleteOldLogSyncEvents() {
        Instant date = Instant.now().minus(removeEventInSeconds, ChronoUnit.SECONDS);
        logSyncEventRepository.deleteEventsOlderThan(date);
    }

    public void deleteOldLogCronJobEntries() {
        Instant date = Instant.now().minus(removeEventInSeconds, ChronoUnit.SECONDS);
        logCronJobEntriesRepository.deleteDataOlderThan(date);
    }

}
