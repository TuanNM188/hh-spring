package com.formos.huub.domain.response.portals;

import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseAboutPortal {

    private UUID id;

    private String platformName;

    private String primaryLogo;

    private String aboutPageContent;

    private List<ResponseAboutPortalHost> portalHosts;
}
