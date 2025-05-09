package com.formos.huub.domain.response.communitypartner;

import com.formos.huub.domain.enums.UserStatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ResponseTechnicalAdvisor {
    private UUID id;

    private String firstName;

    private String lastName;

    private String email;

    private UserStatusEnum status;

    private String normalizedFullName;

    private String imageUrl;

    private List<UUID> portalIds;
}
