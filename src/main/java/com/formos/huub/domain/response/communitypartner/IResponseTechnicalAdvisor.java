package com.formos.huub.domain.response.communitypartner;

import com.formos.huub.domain.enums.UserStatusEnum;

import java.util.UUID;


public interface IResponseTechnicalAdvisor {

     UUID getId();

     String getFirstName();

     String getLastName();

     String getEmail();

     UserStatusEnum getStatus();

     String getNormalizedFullName();

     String getImageUrl();

     String getRole();

}
