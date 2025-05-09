/**
 * ***************************************************
 * * Description :
 * * File        : RequestBasePortal
 * * Author      : Hung Tran
 * * Date        : Nov 12, 2024
 * ***************************************************
 **/
package com.formos.huub.domain.request.portals;

import jakarta.validation.Valid;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestBasePortal {

    @Valid
    private RequestPortalAbout portalAbout;

    @Valid
    private RequestPortalCustomize portalCustomize;

    private RequestPortalProgram portalProgramRequest;

    private List<RequestPortalFeature> portalFeatures;

    @Valid
    private RequestPortalLocation portalLocation;

    @Valid
    private List<RequestPortalHost> portalHosts;

    @Valid
    private RequestPortalWelcomeMessage portalWelcomeMessage;

}
