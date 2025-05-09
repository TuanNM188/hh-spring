package com.formos.huub.domain.request.member;

import com.formos.huub.framework.validation.constraints.RequireCheck;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
public class RequestTechnicalAdvisorSetting {

    @RequireCheck
    private String communityPartnerId;

    @NotEmpty
    private List<UUID> portalIds;

}
