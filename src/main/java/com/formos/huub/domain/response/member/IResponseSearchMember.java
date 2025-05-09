package com.formos.huub.domain.response.member;

import java.time.Instant;
import java.util.UUID;

public interface IResponseSearchMember {
    UUID getId();

    String getImageUrl();

    String getNormalizedFullName();

    String getOrganization();

    String getRole();

    String getStatus();

    String getFollowStatus();

    Boolean getIsPortalHostPrimary();

    Integer getFollowers();

    String getEmail();

    Instant getLastActivity();

    String getUsername();
}
