package com.formos.huub.domain.request.vendor;

import com.formos.huub.domain.request.common.SearchConditionRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RequestSearchVendor extends SearchConditionRequest {
    private UUID portalId;

    private List<UUID> portalIds;

    private UUID communityPartnerId;

    private String searchKeyword;
}
