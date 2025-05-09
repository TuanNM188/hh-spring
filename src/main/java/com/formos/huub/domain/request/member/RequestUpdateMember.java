package com.formos.huub.domain.request.member;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestUpdateMember {

    @Valid
    private RequestMemberProfile memberProfile;

}
