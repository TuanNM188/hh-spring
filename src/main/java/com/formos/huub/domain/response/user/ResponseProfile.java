package com.formos.huub.domain.response.user;

import com.formos.huub.domain.enums.PhoneTypeEnum;
import com.formos.huub.domain.response.common.ResponseUserProfile;
import com.formos.huub.domain.response.member.ResponseTechnicalAdvisor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ResponseProfile extends ResponseUserProfile {

    private String phoneNumber;

    private String extension;

    private PhoneTypeEnum phoneType;

    private String country;

    private String username;

    private ResponseTechnicalAdvisor technicalAdvisor;

}
