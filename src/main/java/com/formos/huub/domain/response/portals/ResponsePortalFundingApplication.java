package com.formos.huub.domain.response.portals;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
public class ResponsePortalFundingApplication {

    private Instant applicationDeadline;

    private Boolean hasDeadline;

    private String applicationUrl;

    private String applicationProcess;

    private String applicationRequirement;

    private String applicationRestriction;

}
