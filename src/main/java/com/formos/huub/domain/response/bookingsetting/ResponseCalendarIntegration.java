package com.formos.huub.domain.response.bookingsetting;

import com.formos.huub.domain.enums.CalendarStatusEnum;
import com.formos.huub.domain.enums.CalendarTypeEnum;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseCalendarIntegration {

    private UUID id;

    private String calendarId;

    private String calendarRefId;

    private CalendarTypeEnum calendarType;

    private String iCalLink;

    private String problem;

    private String email;

    private CalendarStatusEnum calendarStatus;
}
