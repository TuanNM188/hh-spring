package com.formos.huub.domain.response.tasurvey;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
public class ResponseTaSurveyAppointment {
    private UUID appointmentId;
    private UUID businessOwnerId;
    private UUID advisorId;
    private String businessOwnerName;
    private String businessOwnerEmail;
    private String advisorName;
    private String vendorName;
    private Instant appointmentDate;
    private String timezone;
    private Integer rating;
    private String feedback;
}
