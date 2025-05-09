package com.formos.huub.domain.request.communitypartner;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestCommunityPartnerStaff extends RequestVendorTechnicalAdvisor {

    @NotNull
    private Boolean isNavigator;

    @NotNull
    private Boolean isPrimary;

}
