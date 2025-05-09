package com.formos.huub.domain.request.calendarevent;

import com.formos.huub.domain.enums.CalendarTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RequestCalendarEventLink {

    private UUID id;

    private String url;

    private CalendarTypeEnum calendarType;

}
