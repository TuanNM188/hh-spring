package com.formos.huub.domain.request.appointmentmanagement;

import com.formos.huub.framework.validation.constraints.UUIDCheck;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RequestUpdateAppointmentDetail {

    private UUID appointmentId;

    @UUIDCheck
    private String categoryId;

    @UUIDCheck
    private String serviceId;

    private String serviceOutcomes;

    private String supportDescription;
    private String shareLinks;
}
