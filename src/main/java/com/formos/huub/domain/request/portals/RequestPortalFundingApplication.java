package com.formos.huub.domain.request.portals;

import com.formos.huub.framework.validation.constraints.RequireCheck;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
public class RequestPortalFundingApplication {

    private Instant applicationDeadline;

    private Boolean hasDeadline;

    @RequireCheck
    private String applicationUrl;

    @RequireCheck
    private String applicationProcess;

    @RequireCheck
    private String applicationRequirement;

    @RequireCheck
    private String applicationRestriction;

}
