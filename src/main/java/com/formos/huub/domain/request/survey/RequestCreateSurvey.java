package com.formos.huub.domain.request.survey;

import com.formos.huub.framework.validation.constraints.RequireCheck;
import com.formos.huub.framework.validation.constraints.UUIDCheck;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestCreateSurvey {

    @RequireCheck
    private String name;

    private String description;

    @UUIDCheck
    private String portalId;

    private String surveyJson;

    private Boolean isActive;
}
