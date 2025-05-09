package com.formos.huub.domain.response.communitypartner;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ResponseCommunityPartnerAbout {

    private UUID id;

    private String name;

    private String email;

    private String status;

    private String phoneNumber;

    private String website;

    private String address1;

    private String address2;

    private String country;

    private String imageUrl;

    private String city;

    private String state;

    private String zipCode;

    private String bio;

    private String extension;

    private Boolean isVendor;

    private Boolean isActive;

    private String additionalWebsite;

    private String linkedInProfile;

    private String tiktokProfile;

    private String instagramProfile;

    private String facebookProfile;

    private String twitterProfile;

    private List<String> serviceTypes;

    private List<UUID> portals;
}
