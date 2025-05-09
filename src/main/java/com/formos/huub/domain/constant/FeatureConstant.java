package com.formos.huub.domain.constant;

import com.formos.huub.domain.enums.FeatureCodeEnum;

import java.util.Map;

public final class FeatureConstant {

    public static final Map<FeatureCodeEnum, String> GROUP_CODE_PORTAL_INTAKE_QUESTION = Map.of(
        FeatureCodeEnum.PORTALS_INTAKE_QUESTIONS, "Business Owner Intake",
        FeatureCodeEnum.PORTALS_SETTING, "Technical Assistance Application"
    );
}
