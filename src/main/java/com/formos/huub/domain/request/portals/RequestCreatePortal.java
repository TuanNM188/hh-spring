package com.formos.huub.domain.request.portals;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RequestCreatePortal extends RequestBasePortal {

    private List<RequestInvitePortalHost> invitePortalHosts;
}
