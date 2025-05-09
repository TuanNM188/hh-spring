package com.formos.huub.domain.response.account;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseUserSocialLink {

    private String userId;

    private String provider;

    private String attributes;

}
