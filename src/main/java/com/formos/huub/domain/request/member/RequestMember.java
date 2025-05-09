package com.formos.huub.domain.request.member;

import com.formos.huub.domain.entity.User;
import com.formos.huub.domain.request.bookingsetting.RequestBookingSetting;
import com.formos.huub.domain.request.useranswerform.RequestAnswerForm;
import jakarta.validation.Valid;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestMember {

    @Valid
    private RequestMemberProfile memberProfile;

    @Valid
    private RequestTechnicalAdvisor technicalAdvisor;

    @Valid
    private RequestBookingSetting bookingSetting;

    @Valid
    private List<RequestAnswerForm> businessOwner;

    private String resetKey;

    private User loginUser;

    private UUID portalId;

}
