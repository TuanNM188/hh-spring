package com.formos.huub.domain.response.portals;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ResponseAboutPortalHost {

    private UUID id;

    private String firstName;

    private String lastName;

    private String imageUrl;

    private String personalWebsite;

    private String tiktokProfile;

    private String linkedInProfile;

    private String instagramProfile;

    private String facebookProfile;

    private String twitterProfile;

    private String normalizedFullName;
}
