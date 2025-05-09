package com.formos.huub.domain.request.technicalassistance;

import com.formos.huub.domain.enums.ApprovalStatusEnum;
import com.formos.huub.domain.request.common.SearchConditionRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RequestSearchTechnicalApplicationSubmit extends SearchConditionRequest {

    private String searchKeyword;

    private String status;

    private String excludeStatus;

    private UUID portalId;

    private List<UUID> portalIds;

    private UUID communityPartnerId;

}
