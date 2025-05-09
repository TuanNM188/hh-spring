package com.formos.huub.domain.response.calendarintegrate;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
@Setter
public class ResponseOutlookEvents {
    private String odataContext;
    private String odataEtag;
    private String id;
    private String createdDateTime;
    private String lastModifiedDateTime;
    private String changeKey;
    private List<String> categories;
    private String transactionId;
    private String originalStartTimeZone;
    private String originalEndTimeZone;
    private String iCalUId;
    private int reminderMinutesBeforeStart;
    private Boolean isReminderOn;
    private Boolean hasAttachments;
    private String subject;
    private String bodyPreview;
    private String importance;
    private String sensitivity;
    private Boolean isAllDay;
    private Boolean isCancelled;
    private Boolean isOrganizer;
    private Boolean responseRequested;
    private String seriesMasterId;
    private String showAs;
    private String type;
    private String webLink;
    private String onlineMeetingUrl;
    private Boolean isOnlineMeeting;
    private String onlineMeetingProvider;
    private Boolean allowNewTimeProposals;
    private String occurrenceId;
    private Boolean isDraft;
    private Boolean hideAttendees;
    private ResponseStatus responseStatus;
    private Body body;
    private EventTime start;
    private EventTime end;
    private Location location;
    private List<Location> locations;
    private Recurrence recurrence;
    private List<Attendee> attendees;
    private OnlineMeeting onlineMeeting;
    private String errors;
    private HttpStatus status;

    @Getter
    @Setter
    public static class ResponseStatus {
        private String response;
        private String time;
    }

    @Getter
    @Setter
    public static class Body {
        private String contentType;
        private String content;

    }

    @Getter
    @Setter
    public static class EventTime {
        private String dateTime;
        private String timeZone;
    }

    @Getter
    @Setter
    public static class Location {
        private String displayName;
        private String locationType;
        private String uniqueId;
        private String uniqueIdType;
    }

    @Getter
    @Setter
    public static class Recurrence {
        // Define properties for recurrence, if applicable
    }

    @Getter
    @Setter
    public static class Attendee {
        // Define properties for attendee, if applicable
    }


    @Getter
    @Setter
    public static class OnlineMeeting {
        // Define properties for online meeting, if applicable
    }

}
