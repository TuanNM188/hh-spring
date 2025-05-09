package com.formos.huub.domain.response.authenticate;

import com.formos.huub.framework.base.BaseDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * ***************************************************
 * * Description :
 * * File        : SignInResponse
 * * Author      : Hung Tran
 * * Date        : Jun 09, 2024
 * ***************************************************
 **/

@Setter
@Getter
@Builder
public class SignInResponse extends BaseDTO {

    private String accessToken;

    private String refreshToken;

    private String firebaseToken;
}
