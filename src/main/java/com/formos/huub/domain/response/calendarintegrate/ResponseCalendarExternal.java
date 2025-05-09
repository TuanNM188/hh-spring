package com.formos.huub.domain.response.calendarintegrate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseCalendarExternal {
    private String id;
    private String accessRole;
    private String description;
    private String kind;
    private String summary;
    private String timeZone;
}
