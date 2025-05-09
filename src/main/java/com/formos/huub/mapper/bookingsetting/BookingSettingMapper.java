package com.formos.huub.mapper.bookingsetting;

import com.formos.huub.domain.entity.BookingSetting;
import com.formos.huub.domain.request.bookingsetting.RequestBookingSetting;
import com.formos.huub.domain.response.bookingsetting.ResponseSetting;
import com.formos.huub.domain.response.technicaladvisor.ResponseBookingAppointmentForm;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookingSettingMapper {
    BookingSetting toEntity(RequestBookingSetting requestBookingSetting);

    @Mapping(target = "earliestDate", ignore = true)
    void partialEntity(@MappingTarget BookingSetting bookingSetting, RequestBookingSetting requestBookingSetting);

    ResponseSetting toResponse(BookingSetting setting);

    ResponseBookingAppointmentForm toResponseSetting(BookingSetting setting);
}
