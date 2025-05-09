package com.formos.huub.domain.response.communitypartner;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDetailCommunityPartner {

    ResponseCommunityPartnerAbout about;

    ResponseEventSettings eventSettings;

    List<ResponseTechnicalAdvisor> technicalAdvisors;

    List<IResponseCommunityPartnerStaff> communityPartnerStaffs;

}
