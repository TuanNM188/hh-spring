package com.formos.huub.domain.request.communitypartner;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
public class RequestCreateCommunityPartner{

    @NotNull
    private UUID portalId;

    private RequestEventSettings eventSettings;

    private @Valid RequestCommunityPartnerAbout about;

    private @Valid List<RequestVendorTechnicalAdvisor> technicalAdvisors;

    private @Valid List<RequestCommunityPartnerStaff> communityPartnerStaffs;

}
