package com.formos.huub.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.formos.huub.framework.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum FeatureGroupCodeEnum  implements CodeEnum {

    BUSINESS_OWNER_INTAKE("BUSINESS_OWNER_INTAKE", "Business Owner Intake"),
    TECHNICAL_ASSISTANCE_APPLICATION("TECHNICAL_ASSISTANCE_APPLICATION", "Technical Assistance Application");

    private final String value;

    private final String name;

    @JsonValue
    public String getValue() {
        return value;
    }
}
