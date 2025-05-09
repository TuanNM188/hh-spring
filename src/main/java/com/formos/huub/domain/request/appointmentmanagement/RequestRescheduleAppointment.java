package com.formos.huub.domain.request.appointmentmanagement;

import com.formos.huub.framework.validation.constraints.RequireCheck;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestRescheduleAppointment {

    private UUID appointmentId;

    @NotNull
    private Instant appointmentDate;

    @RequireCheck
    private String timezone;
}
