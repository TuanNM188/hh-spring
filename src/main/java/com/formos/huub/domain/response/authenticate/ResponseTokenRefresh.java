package com.formos.huub.domain.response.authenticate;

import com.formos.huub.framework.base.BaseDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Setter
@Getter
public class ResponseTokenRefresh extends BaseDTO {

    private String accessToken;
}
