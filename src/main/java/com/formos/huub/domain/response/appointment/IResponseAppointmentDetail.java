package com.formos.huub.domain.response.appointment;

import com.formos.huub.domain.enums.AppointmentStatusEnum;
import java.time.Instant;
import java.util.UUID;

public interface IResponseAppointmentDetail {
    UUID getId();

    UUID getCategoryId();

    String getCategoryName();

    UUID getServiceId();

    String getServiceName();

    String getSupportDescription();

    String getShareLinks();

    AppointmentStatusEnum getStatus();

    String getSelectedServiceOutcomes();

    String getServiceOutcomes();

    Instant getAppointmentDate();

    String getTimezone();
}
