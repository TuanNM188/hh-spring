package com.formos.huub.domain.request.calendarevent;

import com.formos.huub.domain.enums.IntegrateByEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RequestPortalCalendarEventSetting {

    @NotNull
    private UUID portalId;

    private List<RequestCalendarEventLink> calendarEventSettings;

    private IntegrateByEnum integrateBy;

    private UUID communityPartnerId;

}
