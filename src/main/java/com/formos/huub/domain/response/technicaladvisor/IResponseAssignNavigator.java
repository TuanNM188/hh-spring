package com.formos.huub.domain.response.technicaladvisor;

import java.util.UUID;

public interface IResponseAssignNavigator {

    UUID getUserId();

    String getFullName();

    String getImageUrl();

    UUID getCommunityPartnerId();
    String getCommunityPartnerName();
    String getCommunityPartnerLogo();
    UUID getDirectMessageId();

}
