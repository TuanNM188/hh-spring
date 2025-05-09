package com.formos.huub.domain.response.member;

import com.formos.huub.domain.enums.FollowStatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class ResponsePublicDetailProfile {

    private UUID id;

    private String firstName;

    private String lastName;

    private String username;

    private String imageUrl;

    private String email;

    private String role;

    private String personalWebsite;

    private String tiktokProfile;

    private String linkedInProfile;

    private String instagramProfile;

    private String facebookProfile;

    private String twitterProfile;

    private String portalName;

    private Boolean isBlocked;

    private Instant lastActivityDate;

    private FollowStatusEnum followStatus;

    private Map<String, String> businessInfo;
}
