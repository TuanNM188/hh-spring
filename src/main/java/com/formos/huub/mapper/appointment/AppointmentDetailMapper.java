package com.formos.huub.mapper.appointment;

import com.formos.huub.domain.entity.AppointmentDetail;
import com.formos.huub.domain.request.appointmentmanagement.RequestUpdateAppointmentDetail;
import com.formos.huub.domain.response.appointment.*;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AppointmentDetailMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void partialUpdate(@MappingTarget AppointmentDetail appointmentDetail, RequestUpdateAppointmentDetail request);

    ResponseDetailAppointmentDetail toResponse(IResponseAppointmentDetail request);

    ResponseAppointmentReportDetail toResponse(IResponseAppointmentReportDetail request);
}
