package com.formos.huub.domain.request.technicalassistance;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RequestUpdateTechnicalAssistance {

    private Float assignAwardHours;

    private UUID vendorId;

    private List<UUID> assignAdvisorIds;

}
