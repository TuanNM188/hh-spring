package com.formos.huub.domain.response.businessowner;

import java.util.UUID;

public interface IResponseSearchBusinessOwner {
    UUID getUserId();
    String getBusinessOwnerName();
    String getStatus();
    String getApplicationStatus();
    String getPortalName();
    String getImageUrl();
    String getBusinessName();
}
