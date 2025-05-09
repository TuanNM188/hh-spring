package com.formos.huub.domain.request.portals;

import com.formos.huub.framework.validation.constraints.RequireCheck;
import com.formos.huub.framework.validation.constraints.UUIDCheck;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestUpdatePortalFunding {

    @UUIDCheck
    @RequireCheck
    private String id;

    @UUIDCheck
    @RequireCheck
    private String funderId;

    @Valid
    private RequestPortalFundingAbout portalFundingAbout;

    @Valid
    private RequestPortalFundingApplication portalFundingApplication;

}
