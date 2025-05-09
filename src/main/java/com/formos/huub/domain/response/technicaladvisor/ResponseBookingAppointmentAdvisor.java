package com.formos.huub.domain.response.technicaladvisor;

import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseBookingAppointmentAdvisor {

    private UUID id;

    private UUID technicalAdvisorId;

    private UUID businessOwnerId;

    private String technicalAdvisorName;

    private Instant appointmentDate;

    private String businessOwnerName;

    private String email;

    private String phoneNumber;
}
