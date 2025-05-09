package com.formos.huub.domain.response.communityresource;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ResponseDetailCommunityResource {

    private UUID id;

    private UUID userId;

    private String name;

    private String bio;

    private String imageUrl;

    private String tiktokProfile;

    private String linkedInProfile;

    private String instagramProfile;

    private String facebookProfile;

    private String twitterProfile;

    private String serviceTypes;

    private String personalWebsite;

    private UUID primaryContactId;

    private String primaryContactName;

    private List<ResponseCommunityResourceMember> teamMembers;
}
