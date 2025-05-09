package com.formos.huub.domain.response.appointment;

import com.formos.huub.domain.enums.AppointmentStatusEnum;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseSearchAppointment {

    private UUID id;

    private Instant appointmentDate;

    private String timezone;

    private UUID businessOwnerId;

    private String businessOwnerName;

    private UUID advisorId;

    private String advisorName;

    private UUID navigatorId;

    private String navigatorName;

    private String vendorName;

    private String categoryName;

    private String serviceName;

    private AppointmentStatusEnum status;
}
