/**
 * ***************************************************
 * * Description :
 * * File        : FacebookTokenResponse
 * * Author      : Hung Tran
 * * Date        : Oct 28, 2024
 * ***************************************************
 **/
package com.formos.huub.domain.response.oauth2;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FacebookTokenResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("expires_in")
    private Long expiresIn;
}
