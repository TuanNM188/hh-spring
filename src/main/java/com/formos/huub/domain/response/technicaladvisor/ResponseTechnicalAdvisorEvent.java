package com.formos.huub.domain.response.technicaladvisor;

import com.formos.huub.domain.enums.MeetingPlatformEnum;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseTechnicalAdvisorEvent {

    private UUID id;
    private String title;
    private Instant start;
    private Instant end;
    private String avatar;
    private String linkMeetingPlatform;
    private MeetingPlatformEnum meetingPlatform;
}
