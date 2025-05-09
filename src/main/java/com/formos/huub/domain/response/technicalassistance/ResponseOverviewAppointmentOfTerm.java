package com.formos.huub.domain.response.technicalassistance;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class ResponseOverviewAppointmentOfTerm {

    private UUID programTermId;

    private Instant startDate;

    private Instant endDate;

    private Integer numScheduled;

    private Integer numCompleted;

    private Integer numCanceled;

    private Integer numReportOverdue;
}
