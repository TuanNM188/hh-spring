package com.formos.huub.domain.response.authenticate;

import com.formos.huub.framework.base.BaseDTO;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Setter
@Getter
public class ResponseLogin extends BaseDTO {

    private UUID userId;
    private String username;
    private String password;
    private String accessToken;
    private String refreshToken;
    private String firebaseToken;
}
