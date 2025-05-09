package com.formos.huub.service.externalcalendar;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.io.TimezoneInfo;
import biweekly.property.ExceptionDates;
import biweekly.property.RecurrenceRule;
import biweekly.util.Recurrence;
import biweekly.util.com.google.ical.compat.javautil.DateIterator;
import com.formos.huub.domain.entity.CalendarIntegration;
import com.formos.huub.domain.response.calendarintegrate.ResponseCalendarEvent;
import com.formos.huub.framework.base.BaseService;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.utils.ObjectUtils;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URL;
import java.time.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ICalService extends BaseService {

    private static final String ICAL_URL_REGEX = "/^https?:\\/\\/([a-zA-Z0-9.-]+)\\.[a-zA-Z]{2,}(\\/.*)?\\.ics(\\?.*)?$/gm";


    public List<ResponseCalendarEvent> getEventsFromUrl(String urlString, CalendarIntegration calendarIntegration) {
        if (ObjectUtils.isEmpty(urlString)) {
            return List.of();
        }
        try {
            ICalendar ical = loadICalendarFromUrl(urlString);
            if (ical == null) {
                return List.of();
            }
            String timezoneId = getTimezone(ical.getTimezoneInfo());
            List<ResponseCalendarEvent> events = new ArrayList<>();
            var timeMin = Instant.now().atZone(ZoneOffset.UTC).withHour(0).withMinute(0).withSecond(0);
            var timeMax = timeMin.plusDays(356);
            for (VEvent vEvent : ical.getEvents()) {
                ResponseCalendarEvent event = buildEventFromVEvent(vEvent, timezoneId);
                // Handle recurrence
                RecurrenceRule rrule = vEvent.getRecurrenceRule();
                if (rrule != null) {
                    events.addAll(getRecurringEvents(vEvent, event, timezoneId));
                } else {
                    events.add(event);
                }
            }
            return filterEventsByDateRange(events, timeMin.toInstant(), timeMax.toInstant());

        } catch (Exception e) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0015, "ICal Link"));
        }
    }

    private ICalendar loadICalendarFromUrl(String urlString) throws Exception {
        URI uri = new URI(urlString);
        URL url = uri.toURL();
        return Biweekly.parse(url.openStream()).first();
    }

    private String getTimezone(TimezoneInfo tzinfo) {
        return tzinfo.getTimezones().stream()
            .findFirst()
            .map(tz -> tz.getTimeZone().getID())
            .orElse("UTC");
    }

    private ResponseCalendarEvent buildEventFromVEvent(VEvent vEvent, String timezoneId) {
        var startDate = vEvent.getDateStart().getValue();
        var endDate = vEvent.getDateEnd() != null ? vEvent.getDateEnd().getValue() : startDate;
        var isTimeUtc = vEvent.getDateStart().getValue().getRawComponents().isUtc();
        String description = vEvent.getDescription() != null ? vEvent.getDescription().getValue() : null;
        boolean isAllDay = !startDate.hasTime();
        return buildObjectEvent(
            vEvent.getUid().getValue(),
            vEvent.getSummary().getValue(),
            description,
            getDateEventByDate(startDate, timezoneId, isTimeUtc, false),
            getDateEventByDate(endDate, timezoneId, isTimeUtc, isAllDay),
            timezoneId,
            isAllDay
        );
    }

    private Instant getDateEventByDate(Date date, String timezoneId, boolean isTimeUtc, boolean isAllDay) {
        if (isTimeUtc) {
            return date.toInstant();
        }
        ZonedDateTime dateIns = date.toInstant().atZone(ZoneId.of("UTC"));
        ZonedDateTime pstDateTime = dateIns.withZoneSameLocal(ZoneId.of(timezoneId));
        Instant dateEvent = pstDateTime.toInstant();
        return isAllDay ? dateEvent.minusSeconds(1) : dateEvent;
    }

    private Instant getDateEventByInstant(Instant date, String timezoneId, boolean isTimeUtc) {
        if (isTimeUtc) {
            return date;
        }
        ZonedDateTime dateIns = date.atZone(ZoneId.of("UTC"));
        ZonedDateTime pstDateTime = dateIns.withZoneSameLocal(ZoneId.of(timezoneId));
        return pstDateTime.toInstant();
    }

    private List<ResponseCalendarEvent> getRecurringEvents(VEvent vEvent, ResponseCalendarEvent baseEvent, String timezone) {
        List<ResponseCalendarEvent> recurringEvents = new ArrayList<>();
        RecurrenceRule rrule = vEvent.getRecurrenceRule();
        var isTimeUtc = vEvent.getDateStart().getValue().getRawComponents().isUtc();
        Date startDate = vEvent.getDateStart().getValue().getRawComponents().toDate();
        Date endDate = vEvent.getDateEnd() != null ? vEvent.getDateEnd().getValue().getRawComponents().toDate() : startDate;
        long durationMillis = endDate.getTime() - startDate.getTime();
        Recurrence recurrence = rrule.getValue();
        DateIterator iterator = recurrence.getDateIterator(startDate, TimeZone.getDefault());
        var excludedDates = getExceptionDates(vEvent.getExceptionDates());
        var index = 0;
        while (iterator.hasNext()) {
            var occurrenceStart = iterator.next();
            var occurrenceStartLocalDate = occurrenceStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (excludedDates.contains(occurrenceStartLocalDate)) {
                continue;
            }
            var occurrenceStartDate = combineDateAndTime(startDate.toInstant(), occurrenceStart.toInstant());
            var startTime = getDateEventByInstant(occurrenceStartDate, timezone, isTimeUtc);
            var endTime = startTime.plusMillis(durationMillis);
            var endDateTime = baseEvent.getIsAllDay() ? endTime.minusSeconds(1) : endTime;
            recurringEvents.add(buildObjectEvent(
                baseEvent.getId() + index++,
                baseEvent.getSubject(),
                baseEvent.getSummary(),
                startTime,
                endDateTime,
                timezone,
                baseEvent.getIsAllDay()
            ));
        }

        return recurringEvents;
    }

    private List<LocalDate> getExceptionDates(List<ExceptionDates> exceptionDates) {
        List<LocalDate> excludedDates = new ArrayList<>();
        for (ExceptionDates exdate : exceptionDates) {
            List<LocalDate> dates = exdate.getValues().stream().map(ele -> ele.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()).toList();
            excludedDates.addAll(dates);
        }
        return excludedDates;
    }

    public static Instant combineDateAndTime(Instant sourceInstant, Instant targetInstant) {
        LocalDate targetDate = targetInstant.atZone(ZoneId.systemDefault()).toLocalDate();

        LocalTime sourceTime = sourceInstant.atZone(ZoneId.systemDefault()).toLocalTime();

        LocalDateTime combinedDateTime = LocalDateTime.of(targetDate, sourceTime);

        return combinedDateTime.atZone(ZoneId.systemDefault()).toInstant();
    }

    private List<ResponseCalendarEvent> filterEventsByDateRange(List<ResponseCalendarEvent> events, Instant timeMin, Instant timeMax) {
        return events.stream()
            .filter(event -> event.getStartTime().isAfter(timeMin) && event.getStartTime().isBefore(timeMax))
            .toList();
    }

    private ResponseCalendarEvent buildObjectEvent(String id, String subject, String summary, Instant start, Instant end, String timezone, boolean isAllDay) {
        return ResponseCalendarEvent.builder()
            .id(id)
            .externalEventId(id)
            .subject(subject)
            .summary(summary)
            .description(summary)
            .startTime(start)
            .endTime(end)
            .timezone(timezone)
            .isAllDay(isAllDay)
            .build();
    }

    public boolean isValidICalUrl(String url) {
        Pattern pattern = Pattern.compile(ICAL_URL_REGEX);
        Matcher matcher = pattern.matcher(url);
        return matcher.matches();
    }
}
