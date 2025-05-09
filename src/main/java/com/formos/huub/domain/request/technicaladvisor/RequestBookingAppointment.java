package com.formos.huub.domain.request.technicaladvisor;

import com.formos.huub.framework.validation.constraints.RequireCheck;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestBookingAppointment {

    private UUID businessOwnerId;

    private UUID portalId;

    @NotNull
    private UUID technicalAdvisorId;

    @NotNull
    private Instant appointmentDate;

    @RequireCheck
    private String timezone;

    @NotNull
    private UUID categoryId;

    @NotNull
    private UUID serviceId;

    @RequireCheck
    private String supportDescription;

    @RequireCheck
    private String shareLinks;

    @RequireCheck
    private String serviceOutcomes;

    private Boolean agreeTermAndCondition;
}
