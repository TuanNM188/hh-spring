package com.formos.huub.domain.response.member;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ResponseTechnicalAdvisor {

    private UUID id;

    private ResponseTechnicalAdvisorSetting technicalAdvisorSetting;

}
