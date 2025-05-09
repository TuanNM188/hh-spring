package com.formos.huub.domain.response.portals;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ResponsePortalDetail {

    private String id;

    private String portalId;

    private ResponsePortalAbout portalAbout;

    private ResponsePortalCustomize portalCustomize;

    private ResponsePortalProgram portalProgram;

    private List<ResponsePortalFeature> portalFeatures;

    private ResponsePortalLocation portalLocation;

    private List<ResponsePortalHost> portalHosts;

    private ResponsePortalWelcomeMessage portalWelcomeMessage;

}
