package com.formos.huub.domain.response.appointment;

import com.formos.huub.domain.enums.MeetingPlatformEnum;
import java.time.Instant;
import java.util.UUID;

public interface IResponseAppointmentUpcoming {
    UUID getId();

    Instant getAppointmentDate();

    String getTimezone();

    String getTechnicalAdvisor();

    String getCategory();

    MeetingPlatformEnum getMeetingPlatform();

    String getLinkMeetingPlatform();

    Boolean getIsCancelAppointment();

    Boolean getIsRescheduleAppointment();
}
