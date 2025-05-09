package com.formos.huub.domain.response.member;

import com.formos.huub.domain.enums.PhoneTypeEnum;
import com.formos.huub.domain.enums.UserStatusEnum;
import com.formos.huub.domain.response.common.ResponseUserProfile;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
public class ResponseMemberProfile extends ResponseUserProfile {

    private String role;

    private UserStatusEnum status;

    private String phoneNumber;

    private String extension;

    private PhoneTypeEnum phoneType;

    private String country;

    private UUID portalId;

    private UUID communityPartnerId;

    private Boolean isNavigator;

    private Boolean isPrimary;

}
