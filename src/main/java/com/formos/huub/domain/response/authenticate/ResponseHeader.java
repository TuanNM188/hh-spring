package com.formos.huub.domain.response.authenticate;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Setter
@Getter
public class ResponseHeader {

    private String deviceName;

    private String deviceType;

    private String deviceInfo;

    private String deviceToken;

}
