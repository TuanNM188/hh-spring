package com.formos.huub.domain.response.account;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseSocialUserInfo {

    private String sub;

    private String name;

    private String given_name;

    private String family_name;

    private String picture;

    private String email;

    private Boolean email_verified;

    private String id;

    private String displayName;

    private String givenName;

    private String jobTitle;

    private String mail;

    private String mobilePhone;

    private String officeLocation;

    private String preferredLanguage;

    private String surname;

    private String userPrincipalName;

}
