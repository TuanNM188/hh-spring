package com.formos.huub.domain.response.calendarintegrate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseOutlookCalendar {
    private String id;
    private String name;
    private String changeKey;
    private String canShare;
    private Boolean isRemovable;
}
