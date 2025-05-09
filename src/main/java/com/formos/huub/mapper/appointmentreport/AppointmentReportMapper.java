package com.formos.huub.mapper.appointmentreport;

import com.formos.huub.domain.entity.AppointmentReport;
import com.formos.huub.domain.request.appointmentreport.RequestCreateAppointmentReport;
import com.formos.huub.domain.request.appointmentreport.RequestSubmitAppointmentReport;

import java.util.UUID;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AppointmentReportMapper {
    @Mapping(target = "feedback", source = "request.feedback")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "appointment.id", source = "appointmentId")
    @Mapping(
        target = "serviceOutcomes",
        expression = "java(com.formos.huub.framework.utils.StringUtils.convertListToString(request.getServiceOutcomes()))"
    )
    AppointmentReport toEntity(RequestCreateAppointmentReport request, UUID appointmentId);

    @Mapping(target = "feedback", source = "request.feedback")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "appointment.id", source = "appointmentId")
    @Mapping(target = "businessOwnerAttended", source = "request.businessOwnerAttended", defaultValue = "true")
    @Mapping(
        target = "serviceOutcomes",
        expression = "java(com.formos.huub.framework.utils.StringUtils.convertListToString(request.getServiceOutcomes()))"
    )
    AppointmentReport toEntity(RequestSubmitAppointmentReport request, UUID appointmentId);

}
