package com.formos.huub.domain.request.tasurvey;

import com.formos.huub.domain.request.common.SearchConditionRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RequestSearchTaSurveyProject extends SearchConditionRequest {

    private UUID portalId;
    private UUID communityPartnerId;
    private String searchKeyword;
    private List<UUID> portalIds;
}
