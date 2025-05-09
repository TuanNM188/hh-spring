package com.formos.huub.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.formos.huub.framework.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ServiceTypeEnum  implements CodeEnum {

    FREE_CONSULTANTS("FREE_CONSULTANTS", "Free Consultants"),
    TOOLS("TOOLS", "Tools"),
    NETWORKING_OR_ADVOCACY("NETWORKING_OR_ADVOCACY", "Networking/Advocacy"),
    INCUBATORS_AND_ACCELERATORS("INCUBATORS_AND_ACCELERATORS", "Incubators & Accelerators");

    private final String value;

    private final String name;

    @JsonValue
    public String getValue() {
        return value;
    }
}
