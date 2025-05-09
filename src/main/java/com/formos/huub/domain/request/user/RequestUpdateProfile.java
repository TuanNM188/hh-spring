package com.formos.huub.domain.request.user;

import com.formos.huub.config.Constants;
import com.formos.huub.domain.enums.PhoneTypeEnum;
import com.formos.huub.domain.request.common.RequestUserProfile;
import com.formos.huub.framework.validation.constraints.EnumCheck;
import com.formos.huub.framework.validation.constraints.RequireCheck;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RequestUpdateProfile extends RequestUserProfile {

    @RequireCheck
    private String phoneNumber;

    private String extension;

    @RequireCheck
    @EnumCheck(enumClass = PhoneTypeEnum.class)
    private String phoneType;

    @Pattern(regexp = Constants.USERNAME_REGEX)
    private String username;

}
