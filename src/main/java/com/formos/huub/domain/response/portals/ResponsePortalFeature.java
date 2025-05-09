package com.formos.huub.domain.response.portals;

import com.formos.huub.domain.enums.FeatureCodeEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ResponsePortalFeature {

    private UUID id;

    private String name;

    private Boolean isActive;

    private FeatureCodeEnum featureCode;

    private String featureKey;

}
