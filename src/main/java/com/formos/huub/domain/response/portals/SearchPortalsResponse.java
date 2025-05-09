package com.formos.huub.domain.response.portals;

import com.formos.huub.domain.enums.PortalStatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class SearchPortalsResponse {

    private UUID id;

    private String platformName;

    private String regionName;

    private String state;

    private String city;

    private PortalStatusEnum status;

    private Instant contractYearStartDate;

    private Instant contractYearEndDate;
}
