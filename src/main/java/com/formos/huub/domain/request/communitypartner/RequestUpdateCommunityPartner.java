package com.formos.huub.domain.request.communitypartner;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RequestUpdateCommunityPartner {

    @NotNull
    private UUID portalId;

    private @Valid RequestCommunityPartnerAbout about;

    private RequestEventSettings eventSettings;

    private List<RequestVendorTechnicalAdvisor> technicalAdvisors;

    private @Valid List<RequestCommunityPartnerStaff> communityPartnerStaffs;

}
