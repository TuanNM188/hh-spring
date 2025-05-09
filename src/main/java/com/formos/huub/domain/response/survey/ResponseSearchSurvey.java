package com.formos.huub.domain.response.survey;

import java.util.UUID;
import lombok.Data;

@Data
public class ResponseSearchSurvey {

    private UUID id;

    private String name;

    private String description;

    private Boolean isActive;

    private String portalName;

    private Integer responses;

    private String portalUrl;

    private Boolean isCustomDomain;
}
