package com.formos.huub.domain.request.authenticate;

import com.formos.huub.framework.validation.constraints.RequireCheck;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RequestSwitchPortal {

    @RequireCheck
    private String userName;

    @RequireCheck
    private UUID portalId;

    private String adminUser;

    private String adminUrl;
}
