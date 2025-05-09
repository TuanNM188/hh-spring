package com.formos.huub.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.formos.huub.framework.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ProblemTypeEnum implements CodeEnum {
    UNSUPPORTED_PORTAL_SWITCH_TO_PORTAL_SUPPORTED("UNSUPPORTED_PORTAL_SWITCH_TO_PORTAL_SUPPORTED", "Unsupported portal switch to supported portal"),
    PORTAL_NOT_SUPPORTED("PORTAL_NOT_SUPPORTED", "Portal not supported.");

    private final String value;

    private final String name;

    @JsonValue
    public String getValue() {
        return value;
    }
}

