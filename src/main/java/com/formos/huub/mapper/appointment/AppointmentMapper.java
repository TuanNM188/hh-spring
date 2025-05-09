package com.formos.huub.mapper.appointment;

import com.formos.huub.domain.entity.Appointment;
import com.formos.huub.domain.entity.AppointmentDetail;
import com.formos.huub.domain.request.appointmentmanagement.RequestRescheduleAppointment;
import com.formos.huub.domain.request.appointmentmanagement.RequestUpdateAppointmentDetail;
import com.formos.huub.domain.request.technicaladvisor.RequestBookingAppointment;
import com.formos.huub.domain.response.appointment.ResponseAppointment;
import com.formos.huub.domain.response.technicaladvisor.ResponseAppointmentBooked;
import com.formos.huub.domain.response.technicaladvisor.ResponseBookingAppointmentAdvisor;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AppointmentMapper {
    Appointment toEntity(RequestBookingAppointment request);

    @Mapping(target = "technicalAdvisorId", source = "technicalAdvisor.id")
    @Mapping(target = "technicalAdvisorName", source = "technicalAdvisor.user.normalizedFullName")
    ResponseBookingAppointmentAdvisor toResponseBookingAppointmentAdvisor(Appointment appointment);


    ResponseAppointmentBooked toResponseAppointmentBooked(Appointment appointment);

    @Mapping(target = "technicalAdvisorId", source = "technicalAdvisor.id")
    @Mapping(target = "technicalAdvisorName", source = "technicalAdvisor.user.normalizedFullName")
    @Mapping(target = "businessOwnerId", source = "user.id")
    @Mapping(target = "businessOwnerName", source = "user.normalizedFullName")
    @Mapping(target = "appointmentDetail.id", source = "appointmentDetail.id")
    @Mapping(target = "appointmentDetail.categoryId", source = "appointmentDetail.categoryId")
    @Mapping(target = "appointmentDetail.serviceId", source = "appointmentDetail.serviceId")
    @Mapping(target = "appointmentDetail.supportDescription", source = "appointmentDetail.supportDescription")
    @Mapping(target = "appointmentDetail.shareLinks", source = "appointmentDetail.shareLinks")
    @Mapping(target = "appointmentDetail.serviceOutcomes", source = "appointmentDetail.serviceOutcomes")
    @Mapping(target = "appointmentDetail.rating", source = "appointmentDetail.rating")
    @Mapping(target = "appointmentDetail.feedback", source = "appointmentDetail.feedback")
    @Mapping(target = "appointmentDetail.comments", source = "appointmentDetail.comments")
    @Mapping(target = "appointmentDetail.useAwardHours", source = "appointmentDetail.useAwardHours")
    ResponseAppointment toResponse(Appointment appointment);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void partialUpdate(@MappingTarget Appointment appointment, RequestRescheduleAppointment request);
}
