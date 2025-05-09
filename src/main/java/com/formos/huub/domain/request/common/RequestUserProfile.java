package com.formos.huub.domain.request.common;

import com.formos.huub.domain.enums.UserStatusEnum;
import com.formos.huub.framework.validation.constraints.RequireCheck;
import com.formos.huub.framework.validation.constraints.UUIDCheck;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RequestUserProfile {

    @UUIDCheck
    private String id;

    @RequireCheck
    @Size(max = 50)
    private String firstName;

    @RequireCheck
    @Size(max = 50)
    private String lastName;

    @RequireCheck
    @Size(max = 50)
    @Email
    private String email;

    @RequireCheck
    private String country;

    @RequireCheck
    private String address1;

    private String address2;

    private String appt;

    @RequireCheck
    private String city;

    private String state;

    @RequireCheck
    @Size(max = 10)
    private String zipCode;

    private String imageUrl;

    @RequireCheck
    private String bio;

    private String personalWebsite;

    private String tiktokProfile;

    private String linkedInProfile;

    private String instagramProfile;

    private String facebookProfile;

    private String twitterProfile;

    private UserStatusEnum profileStatus;

}
