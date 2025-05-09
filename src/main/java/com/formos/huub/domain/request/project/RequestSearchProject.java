package com.formos.huub.domain.request.project;

import com.formos.huub.domain.enums.ProjectStatusEnum;
import com.formos.huub.domain.request.common.SearchConditionRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RequestSearchProject extends SearchConditionRequest {

    private UUID portalId;

    private List<UUID> portalIds;

    private UUID technicalAssistanceId;

    private UUID communityPartnerId;

    private UUID technicalAdvisorId;

    private String status;

    private List<String> projectStatus;

    private String searchKeyword;

    private Boolean isCurrent;

    private String timezone;
}
