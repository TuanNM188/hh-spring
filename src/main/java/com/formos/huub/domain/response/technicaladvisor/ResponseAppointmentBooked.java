package com.formos.huub.domain.response.technicaladvisor;

import com.formos.huub.domain.enums.AppointmentStatusEnum;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseAppointmentBooked {

    private Instant appointmentDate;

    private AppointmentStatusEnum status;
}
