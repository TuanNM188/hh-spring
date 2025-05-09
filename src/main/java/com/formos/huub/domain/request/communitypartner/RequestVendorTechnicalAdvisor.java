package com.formos.huub.domain.request.communitypartner;

import com.formos.huub.domain.enums.UserStatusEnum;
import com.formos.huub.framework.validation.constraints.RequireCheck;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RequestVendorTechnicalAdvisor {

    @NotNull
    private UUID id;

    @RequireCheck
    private String firstName;

    @RequireCheck
    private String lastName;

    @RequireCheck
    @Email
    private String email;

    private UserStatusEnum status;

    private List<UUID> portalIds;
}
