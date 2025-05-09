package com.formos.huub.domain.response.portals;

import com.formos.huub.domain.enums.UserStatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ResponseExistPortalHost {

    private Boolean isExist;

    private String email;

    private String firstName;

    private String lastName;

    private String portalName;

    private UserStatusEnum status;

    private UUID userId;
}
