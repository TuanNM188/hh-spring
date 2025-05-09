package com.formos.huub.domain.request.communitypartner;

import com.formos.huub.domain.enums.ServiceTypeEnum;
import com.formos.huub.framework.validation.constraints.RequireCheck;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
public class RequestCommunityPartnerAbout {

    @RequireCheck
    private String name;

    @RequireCheck
    @Size(max = 50)
    @Email
    private String email;

    private String status;

    @RequireCheck
    private String phoneNumber;

    @RequireCheck
    private String website;

    @RequireCheck
    private String address1;

    @RequireCheck
    private String imageUrl;

    private String address2;

    @RequireCheck
    private String country;

    @RequireCheck
    private String city;

    private String state;

    @RequireCheck
    private String zipCode;

    @RequireCheck
    private String bio;

    private String extension;

    @NotNull
    private Boolean isVendor;

    @NotNull
    private Boolean isActive;

    private String additionalWebsite;

    private String tiktokProfile;

    private String linkedInProfile;

    private String instagramProfile;

    private String facebookProfile;

    private String twitterProfile;

    @NotNull
    private List<ServiceTypeEnum> serviceTypes;

    private List<UUID> portals;

    public String toServiceTypes() {
        if (Objects.isNull(this.serviceTypes)) {
            return null;
        }
        return this.serviceTypes.stream().map(ServiceTypeEnum::getValue).collect(Collectors.joining(","));
    };
}
