package com.formos.huub.domain.request.portals;

import com.formos.huub.domain.request.common.SearchConditionRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RequestSearchPortalFunding extends SearchConditionRequest {
    private UUID portalId;
}
