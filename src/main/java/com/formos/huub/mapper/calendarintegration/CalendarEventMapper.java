package com.formos.huub.mapper.calendarintegration;

import com.formos.huub.domain.entity.CalendarEvent;
import com.formos.huub.domain.request.calendarevent.RequestCreateEvent;
import com.formos.huub.domain.request.calendarevent.RequestUpdateEvent;
import com.formos.huub.domain.response.calendarevent.ResponseCalendarEventDetail;
import com.formos.huub.domain.response.calendarintegrate.ResponseCalendarEvent;
import com.formos.huub.domain.response.eventcalendar.ResponseListEventCalendar;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CalendarEventMapper {

    @Mapping(target = "externalEventId", source = "id")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "description", source = "summary")
    CalendarEvent toEntity(ResponseCalendarEvent  event);

    @Mapping(target = "description", source = "summary")
    CalendarEvent toEntityEvent(RequestCreateEvent requestCreateEvent);

    @Mapping(target = "id", ignore = true)
    void partialEntity(@MappingTarget CalendarEvent event, ResponseCalendarEvent calendarEvent);

    @Mapping(target = "description", source = "summary")
    void partialEntity(@MappingTarget CalendarEvent event, RequestUpdateEvent requestUpdateEvent);

    @Mapping(target = "description", source = "summary")
    ResponseCalendarEventDetail toResponseEvent(CalendarEvent event);

    ResponseListEventCalendar toResponseList(CalendarEvent event);
}
