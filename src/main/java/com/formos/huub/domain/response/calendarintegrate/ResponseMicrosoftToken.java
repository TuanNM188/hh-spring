package com.formos.huub.domain.response.calendarintegrate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseMicrosoftToken {

    private String token_type;

    private String scope;

    private Long expires_in;

    private Long ext_expires_in;

    private String access_token;

    private String refresh_token;

    private String id_token;
}
