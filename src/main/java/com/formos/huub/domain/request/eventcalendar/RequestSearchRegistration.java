package com.formos.huub.domain.request.eventcalendar;

import com.formos.huub.domain.request.common.SearchConditionRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RequestSearchRegistration extends SearchConditionRequest {

    private UUID portalId;

    private String timezone;
}
