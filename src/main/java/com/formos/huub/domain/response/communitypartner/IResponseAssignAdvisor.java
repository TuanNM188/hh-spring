package com.formos.huub.domain.response.communitypartner;

import java.time.Instant;
import java.util.UUID;


public interface IResponseAssignAdvisor {

    UUID getId();

    UUID getUserId();

    String getName();

    Instant getCreatedDate();

    String getFirstName();

    String getLastName();

    String getEmail();

    String getPhoneNumber();

    UUID getApplicationId();

}
