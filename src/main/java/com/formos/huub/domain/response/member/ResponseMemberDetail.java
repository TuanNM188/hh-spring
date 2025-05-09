package com.formos.huub.domain.response.member;

import com.formos.huub.domain.response.bookingsetting.ResponseSetting;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseMemberDetail {

    private ResponseMemberProfile memberProfile;

    private ResponseTechnicalAdvisor technicalAdvisor;

    private ResponseSetting bookingSetting;

    private ResponseBusinessOwnerQuestionForm businessOwner;

    private Boolean isPortalHostPrimary;

}
