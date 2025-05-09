package com.formos.huub.web.rest.v1.APIPublic;

import com.formos.huub.domain.request.eventcalendar.RequestSearchEvents;
import com.formos.huub.domain.response.calendarevent.RequestSearchCalendarEvents;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.framework.utils.HeaderUtils;
import com.formos.huub.service.calendarevent.CalendarEventService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/public/calendars")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CalendarPublicController {

    ResponseSupport responseSupport;
    CalendarEventService calendarEventService;

    @PostMapping("/list")
    public ResponseEntity<ResponseData> searchCalendarEvents(
        @RequestBody RequestSearchCalendarEvents request,
        HttpServletRequest httpServletRequest
    ) {
        var response = calendarEventService.searchPortalCalendarEvents(request, HeaderUtils.getTimezone(httpServletRequest));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @GetMapping("/view")
    public ResponseEntity<ResponseData> searchCalendarEventsByDate(RequestSearchEvents request, HttpServletRequest httpServletRequest) {
        var response = calendarEventService.searchEventsByDate(request, HeaderUtils.getTimezone(httpServletRequest));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }
}
