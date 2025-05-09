package com.formos.huub.domain.response.technicalassistance;

import java.time.Instant;
import java.util.UUID;

public interface IResponseOverviewAppointmentOfTerm {

     UUID getProgramTermId();

     Instant getStartDate();

     Instant getEndDate();

     Integer getNumScheduled();

     Integer getNumCompleted();

     Integer getNumCanceled();

     Integer getNumReportOverdue();
}
