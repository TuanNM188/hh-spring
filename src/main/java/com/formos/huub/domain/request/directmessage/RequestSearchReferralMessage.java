package com.formos.huub.domain.request.directmessage;

import com.formos.huub.domain.request.common.SearchConditionRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RequestSearchReferralMessage extends SearchConditionRequest {

    private UUID portalId;

    private UUID businessOwnerId;

    private String regexHtml;

    private List<UUID> portalIds;
}
