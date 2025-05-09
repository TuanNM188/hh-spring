package com.formos.huub.domain.request.survey;

import com.formos.huub.framework.validation.constraints.UUIDCheck;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestUpdateStatusSurvey {

    @UUIDCheck
    private String id;

    private Boolean isActive;
}
