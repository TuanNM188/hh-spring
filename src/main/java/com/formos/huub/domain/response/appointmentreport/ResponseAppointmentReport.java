package com.formos.huub.domain.response.appointmentreport;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class ResponseAppointmentReport {
    private UUID appointmentReportId;
    private Float remainingHours;
    private Instant programTermStartDate;
    private Instant programTermEndDate;
}
