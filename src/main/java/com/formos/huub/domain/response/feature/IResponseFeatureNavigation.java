package com.formos.huub.domain.response.feature;

import com.formos.huub.domain.enums.FeatureCodeEnum;

import java.util.UUID;

public interface IResponseFeatureNavigation {

    UUID getId();

    String getName();

    String getDescription();

    FeatureCodeEnum getFeatureCode();

    String getRouterLink();

    Integer getPriorityOrder();

    Boolean getIsActive();

    FeatureCodeEnum getParentFeatureCode();

    String getGroupCode();

    String getGroupName();

    String getFeatureKey();

    String getGroupKey();
}
