package com.formos.huub.domain.response.appointment;

import com.formos.huub.domain.enums.MeetingPlatformEnum;
import java.time.Instant;
import java.util.UUID;

public interface IResponseHeaderAppointmentDetail {
    UUID getId();

    String getAdvisorName();

    UUID getAdvisorId();

    String getAdvisorImageUrl();

    Instant getAppointmentDate();

    String getTimezone();

    String getLinkMeetingPlatform();

    MeetingPlatformEnum getMeetingPlatform();
}
