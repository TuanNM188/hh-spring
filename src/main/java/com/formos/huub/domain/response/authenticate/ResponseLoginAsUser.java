package com.formos.huub.domain.response.authenticate;

import com.formos.huub.framework.base.BaseDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Builder
@Setter
@Getter
public class ResponseLoginAsUser extends BaseDTO {
    private String username;
    private String password;
    private String accessToken;
    private String refreshToken;
    private String adminUser;
    private String adminUrl;
    private UUID userId;
    private String firebaseToken;
}
