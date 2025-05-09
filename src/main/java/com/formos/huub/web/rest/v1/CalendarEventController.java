package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.request.calendarevent.RequestCreateEvent;
import com.formos.huub.domain.request.calendarevent.RequestPortalCalendarEventSetting;
import com.formos.huub.domain.request.calendarevent.RequestUpdateEvent;
import com.formos.huub.domain.request.eventcalendar.RequestSearchEvents;
import com.formos.huub.domain.response.calendarevent.RequestSearchCalendarEvents;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.framework.utils.HeaderUtils;
import com.formos.huub.service.calendarevent.CalendarEventService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/calendar-events")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CalendarEventController {

    CalendarEventService calendarEventService;

    ResponseSupport responseSupport;

    @PostMapping("/search")
    @PreAuthorize("hasPermission(null, 'SEARCH_CALENDAR_EVENTS')")
    public ResponseEntity<ResponseData> searchCalendarEvents(@RequestBody RequestSearchCalendarEvents request, HttpServletRequest httpServletRequest) {
        var response = calendarEventService.searchPortalCalendarEvents(request,HeaderUtils.getTimezone(httpServletRequest));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @GetMapping("/calendar-view")
    @PreAuthorize("hasPermission(null, 'SEARCH_CALENDAR_EVENTS')")
    public ResponseEntity<ResponseData> searchCalendarEventsByDate(RequestSearchEvents request, HttpServletRequest httpServletRequest) {
        var response = calendarEventService.searchEventsByDate(request,HeaderUtils.getTimezone(httpServletRequest));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @GetMapping("/{eventId}/related")
    @PreAuthorize("hasPermission(null, 'SEARCH_CALENDAR_EVENTS')")
    public ResponseEntity<ResponseData> getRelatedByEvent(@PathVariable @Valid String eventId) {
        var response = calendarEventService.getEventRelated(UUID.fromString(eventId));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }


    @PostMapping
    @PreAuthorize("hasPermission(null, 'CREATE_CALENDAR_EVENTS')")
    public ResponseEntity<ResponseData> createEvent(@RequestBody @Valid RequestCreateEvent request, HttpServletRequest httpServletRequest) {
        request.setTimezone(HeaderUtils.getTimezone(httpServletRequest));
        calendarEventService.createEvent(request);
        return responseSupport.success(ResponseData.builder().build());
    }

    @GetMapping("/{eventId}")
    @PreAuthorize("hasPermission(null, 'GET_DETAIL_CALENDAR_EVENT_BY_ID')")
    public ResponseEntity<ResponseData> getDetailById(@PathVariable @Valid String eventId) {
        var response = calendarEventService.getDetailEvent(UUID.fromString(eventId));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasPermission(null, 'DELETE_CALENDAR_EVENT_BY_ID')" +
        " or @ownerCheckSecurity.isCommunityPartnerOwnerEvent(#eventId, authentication)")
    public ResponseEntity<ResponseData> deleteEvent(@PathVariable @Valid String eventId) {
        calendarEventService.deleteEventById(UUID.fromString(eventId));
        return responseSupport.success(ResponseData.builder().build());
    }

    @PutMapping("/{eventId}")
    @PreAuthorize("hasPermission(null, 'UPDATE_CALENDAR_EVENT_BY_ID') " +
        "or @ownerCheckSecurity.isCommunityPartnerOwnerEvent(#eventId, authentication)")
    public ResponseEntity<ResponseData> updateEvent(@RequestBody RequestUpdateEvent request, @PathVariable @Valid String eventId,
                                                    HttpServletRequest httpServletRequest) {
        request.setTimezone(HeaderUtils.getTimezone(httpServletRequest));
        calendarEventService.updateEvent(UUID.fromString(eventId), request);
        return responseSupport.success(ResponseData.builder().build());
    }

    @GetMapping("/portal-setting/{portalId}")
    @PreAuthorize("hasPermission(null, 'GET_CALENDAR_EVENTS_BY_PORTAL')")
    public ResponseEntity<ResponseData> getPortalSettings(@PathVariable String portalId) {
        var response = calendarEventService.getCalendarEventSettingForPortal(UUID.fromString(portalId));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PostMapping("/portal-setting")
    @PreAuthorize("hasPermission(null, 'UPDATE_PORTAL_SETTINGS_BY_PORTAL')")
    public ResponseEntity<ResponseData> savePortalSettings(@RequestBody @Valid RequestPortalCalendarEventSetting request) {
        calendarEventService.saveCalendarEventSettings(request);
        return responseSupport.success(ResponseData.builder().build());
    }
}
