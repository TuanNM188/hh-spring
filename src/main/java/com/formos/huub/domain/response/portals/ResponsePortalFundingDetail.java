package com.formos.huub.domain.response.portals;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponsePortalFundingDetail {

    private String id;

    private String funderId;

    private ResponsePortalFundingAbout portalFundingAbout;

    private ResponsePortalFundingApplication portalFundingApplication;

}
