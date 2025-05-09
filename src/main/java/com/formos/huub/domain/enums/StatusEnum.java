package com.formos.huub.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.formos.huub.framework.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum StatusEnum  implements CodeEnum {

    ACTIVE("ACTIVE", "ACTIVE"),
    ENDED("ENDED", "ENDED"),
    INACTIVE("INACTIVE", "INACTIVE");

    private final String value;

    private final String name;

    @JsonValue
    public String getValue() {
        return value;
    }
}
