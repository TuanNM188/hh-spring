package com.formos.huub.domain.request.portals;

import com.formos.huub.framework.validation.constraints.RequireCheck;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RequestInvitePortalHost {

    private UUID portalHostId;

    private String portalName;

    @RequireCheck
    private String email;

    @RequireCheck
    private String firstName;

    @RequireCheck
    private String lastName;

    private UUID portalId;

    private String portalUrl;

}
