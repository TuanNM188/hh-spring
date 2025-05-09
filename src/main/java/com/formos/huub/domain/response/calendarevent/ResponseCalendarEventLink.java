package com.formos.huub.domain.response.calendarevent;

import com.formos.huub.domain.enums.CalendarTypeEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ResponseCalendarEventLink {

    private UUID id;

    private String url;

    private CalendarTypeEnum calendarType;

    private Integer priorityOrder;
}
