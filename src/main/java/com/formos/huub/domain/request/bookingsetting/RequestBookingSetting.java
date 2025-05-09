package com.formos.huub.domain.request.bookingsetting;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RequestBookingSetting {

    private UUID userId;

    private boolean allowBooking;

    private String timezone;

    private Instant earliestDate;

    private String meetingPlatform;

    private String linkMeetingPlatform;

    private List<RequestTechnicalAdvisorAvailability> availabilities;

    private RequestConnectCalendar calendarIntegrate;

}
