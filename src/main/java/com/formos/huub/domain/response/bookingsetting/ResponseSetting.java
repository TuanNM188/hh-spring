package com.formos.huub.domain.response.bookingsetting;

import com.formos.huub.domain.enums.MeetingPlatformEnum;
import com.formos.huub.domain.response.calendarintegrate.ResponseCalendarExternal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseSetting {

    private UUID id;

    private UUID userId;

    private Boolean allowBooking;

    private Instant earliestDate;

    private String timezone;

    private MeetingPlatformEnum meetingPlatform;

    private String linkMeetingPlatform;

    private List<ResponseAvailability> availabilities;

    private ResponseCalendarIntegration calendarIntegration;

    private List<ResponseCalendarExternal> calendarExternals;

    public ResponseSetting(UUID userId) {
        this.userId = userId;
    }
}
