package com.formos.huub.domain.response.eventregistration;

import com.formos.huub.domain.enums.EventRegistrationStatusEnum;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseEventRegistration implements Serializable {

    private String externalEventId;
    private String externalAttendeeId;

    private String emailBO;

    private Instant created;

    private EventRegistrationStatusEnum status;

    private Boolean isCheckedIn;
    private Boolean isCancelled;
    private Boolean isRefunded;
    private UUID calendarEventId;
}
