package com.formos.huub.domain.request.survey;

import com.formos.huub.framework.validation.constraints.RequireCheck;
import com.formos.huub.framework.validation.constraints.UUIDCheck;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestUpdateSurvey {

    @UUIDCheck
    private String id;

    @RequireCheck
    private String name;

    private String description;

    @UUIDCheck
    private String portalId;

    private String surveyJson;
}
