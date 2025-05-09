package com.formos.huub.domain.response.calendarintegrate;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ResponseOutlookListCalendar {

    private List<ResponseOutlookCalendar> value;
}
