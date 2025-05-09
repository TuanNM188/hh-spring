package com.formos.huub.domain.request.communityresource;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RequestSearchCommunityResource {

    private UUID portalId;

    private String serviceTypes;
}
