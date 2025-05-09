package com.formos.huub.domain.response.appointment;

import com.formos.huub.domain.enums.AppointmentStatusEnum;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseAppointment {

    private UUID id;

    private UUID technicalAdvisorId;

    private String technicalAdvisorName;

    private UUID businessOwnerId;

    private String businessOwnerName;

    private Instant appointmentDate;

    private String timezone;

    private AppointmentStatusEnum status;

    private ResponseAppointmentDetail appointmentDetail;
}
