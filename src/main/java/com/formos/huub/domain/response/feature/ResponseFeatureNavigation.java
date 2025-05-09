package com.formos.huub.domain.response.feature;

import com.formos.huub.domain.enums.FeatureCodeEnum;
import com.formos.huub.domain.enums.FeatureGroupCodeEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ResponseFeatureNavigation {

    private UUID id;

    private String name;

    private String description;

    private FeatureCodeEnum featureCode;

    private  String routerLink;

    private Integer priorityOrder;

    private Boolean isActive;

    private FeatureGroupCodeEnum groupCode;

    private String groupName;

    private String featureKey;

    private String groupKey;

}
