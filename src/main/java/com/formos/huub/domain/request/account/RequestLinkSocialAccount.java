package com.formos.huub.domain.request.account;

import com.formos.huub.domain.enums.AuthProviderEnum;
import com.formos.huub.framework.validation.constraints.EnumCheck;
import com.formos.huub.framework.validation.constraints.RequireCheck;
import lombok.*;

@Setter
@Getter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class RequestLinkSocialAccount {

    @RequireCheck
    private String accessToken;

    private String code;

    @RequireCheck
    @EnumCheck(enumClass = AuthProviderEnum.class)
    private String provider;

}
