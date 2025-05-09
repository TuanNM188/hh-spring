package com.formos.huub.domain.response.technicaladvisor;

import com.formos.huub.domain.enums.MeetingPlatformEnum;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseBookingAppointmentForm {

    private Instant earliestDate;

    private String timezone;

    private MeetingPlatformEnum meetingPlatform;

    private String linkMeetingPlatform;

    private List<ResponseAdvisorAvailability> availabilities;

    private List<ResponseAppointmentBooked> appointmentBooked;

}
