package com.formos.huub.domain.response.feature;

import com.formos.huub.domain.enums.FeatureCodeEnum;

import java.util.UUID;

public interface IResponseFeaturePortalSetting {

    UUID getId();

    String getName();

    Boolean getIsActive();

    FeatureCodeEnum getFeatureCode();

    String getFeatureKey();

}
