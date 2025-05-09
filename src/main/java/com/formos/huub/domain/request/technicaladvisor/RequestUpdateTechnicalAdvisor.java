package com.formos.huub.domain.request.technicaladvisor;

import com.formos.huub.domain.request.common.RequestUserProfile;
import com.formos.huub.framework.validation.constraints.RequireCheck;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestUpdateTechnicalAdvisor extends RequestUserProfile {

    @RequireCheck
    private String organization;

    @RequireCheck
    private String title;

}
