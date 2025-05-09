package com.formos.huub.domain.request.eventcalendar;

import com.formos.huub.domain.enums.EventStatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RequestSearchEvents {

    private String searchKeyword;

    private String startDate;

    private String endDate;

    private UUID portalId;

    private EventStatusEnum status;
}
