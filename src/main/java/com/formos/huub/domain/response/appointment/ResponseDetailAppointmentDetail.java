package com.formos.huub.domain.response.appointment;

import com.formos.huub.domain.enums.AppointmentStatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
public class ResponseDetailAppointmentDetail {

    private UUID id;

    private UUID categoryId;
    private String categoryName;

    private UUID serviceId;
    private String serviceName;

    private String supportDescription;
    private String shareLinks;

    private String selectedServiceOutcomes;
    private String serviceOutcomes;
    private Boolean isDisplaySubmitReportButton;

    private Boolean isDisplayRescheduleButton;

    private Boolean isDisplayCancelButton;

    private AppointmentStatusEnum status;

    private ResponseAppointmentReportDetail appointmentReport;

}
