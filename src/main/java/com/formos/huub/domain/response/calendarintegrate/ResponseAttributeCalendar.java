package com.formos.huub.domain.response.calendarintegrate;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class ResponseAttributeCalendar {

    private String email;

    private List<ResponseCalendarExternal> calendarExternals;
}
