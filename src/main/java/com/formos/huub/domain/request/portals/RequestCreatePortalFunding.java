package com.formos.huub.domain.request.portals;

import com.formos.huub.framework.validation.constraints.RequireCheck;
import com.formos.huub.framework.validation.constraints.UUIDCheck;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestCreatePortalFunding {

    @UUIDCheck
    @RequireCheck
    private String funderId;

    @Valid
    private RequestPortalFundingAbout portalFundingAbout;

    @Valid
    private RequestPortalFundingApplication portalFundingApplication;

}
