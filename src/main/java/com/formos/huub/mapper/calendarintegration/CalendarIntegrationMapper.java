package com.formos.huub.mapper.calendarintegration;

import com.formos.huub.domain.entity.CalendarIntegration;
import com.formos.huub.domain.request.bookingsetting.RequestConnectCalendar;
import com.formos.huub.domain.response.bookingsetting.ResponseCalendarIntegration;
import com.formos.huub.domain.response.calendarevent.ResponseCalendarEventLink;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CalendarIntegrationMapper {
    CalendarIntegration toEntity(RequestConnectCalendar requestConnectCalendar);

    void partialEntity(@MappingTarget CalendarIntegration calendarIntegration, RequestConnectCalendar request);

    @Mapping(target = "ICalLink", source = "url")
    ResponseCalendarIntegration toResponse(CalendarIntegration calendarIntegration);

    ResponseCalendarEventLink toResponseSetting(CalendarIntegration calendarIntegration);
}
