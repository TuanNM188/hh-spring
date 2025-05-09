package com.formos.huub.domain.response.survey;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseSurvey {

    private UUID id;

    private String name;

    private String description;

    private Boolean isActive;

    private String portalId;

    private String surveyJson;

    private String portalUrl;

    private Boolean isCustomDomain;
}
