package com.formos.huub.repository;

import com.formos.huub.domain.request.appointmentmanagement.RequestSearchAppointment;
import com.formos.huub.domain.request.tasurvey.RequestSearchTaSurveyAppointment;
import com.formos.huub.domain.response.appointment.ResponseAppointmentDetail;
import com.formos.huub.domain.response.appointment.ResponseSearchAppointment;
import com.formos.huub.domain.response.calendarevent.RequestSearchCalendarEvents;
import com.formos.huub.domain.response.calendarevent.ResponseSearchCalendarEvents;
import com.formos.huub.domain.response.tasurvey.ResponseTaSurveyAppointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AppointmentRepositoryCustom {

    Page<ResponseSearchAppointment> searchByTermAndCondition(RequestSearchAppointment condition, Pageable pageable);

    Page<ResponseTaSurveyAppointment> getRatingResponse(RequestSearchTaSurveyAppointment condition, Pageable pageable);
}
