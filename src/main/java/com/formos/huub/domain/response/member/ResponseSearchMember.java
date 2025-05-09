package com.formos.huub.domain.response.member;

import com.formos.huub.domain.enums.UserStatusEnum;
import java.time.Instant;
import java.util.UUID;
import lombok.Data;

@Data
public class ResponseSearchMember {

    private UUID id;

    private String normalizedFullName;

    private String email;

    private String role;

    private Integer followers;

    private String organization;

    private Instant lastActivity;

    private UserStatusEnum status;

    private Boolean isPortalHostPrimary;

    private String username;
}
