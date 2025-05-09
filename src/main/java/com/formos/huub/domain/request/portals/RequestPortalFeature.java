package com.formos.huub.domain.request.portals;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RequestPortalFeature {

    private UUID id;

    private String name;

    private Boolean isActive;

}
