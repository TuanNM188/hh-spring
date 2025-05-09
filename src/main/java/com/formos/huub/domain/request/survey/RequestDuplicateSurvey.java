package com.formos.huub.domain.request.survey;

import com.formos.huub.framework.validation.constraints.UUIDCheck;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestDuplicateSurvey {

    @UUIDCheck
    private String id;
}
