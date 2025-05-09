package com.formos.huub.domain.request.member;

import com.formos.huub.domain.enums.PhoneTypeEnum;
import com.formos.huub.domain.enums.UserStatusEnum;
import com.formos.huub.domain.request.common.RequestUserProfile;
import com.formos.huub.framework.validation.constraints.EnumCheck;
import com.formos.huub.framework.validation.constraints.RequireCheck;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class RequestMemberProfile extends RequestUserProfile {

    @RequireCheck
    private String role;

    @RequireCheck
    @EnumCheck(enumClass = UserStatusEnum.class)
    private String status;

    @RequireCheck
    private String phoneNumber;

    private String extension;

    @RequireCheck
    @EnumCheck(enumClass = PhoneTypeEnum.class)
    private String phoneType;

    private String serviceTypes;

    private String username;

    private UUID portalId;

    private UUID communityPartnerId;

    private Boolean isNavigator;

    private Boolean isPrimary;

}
