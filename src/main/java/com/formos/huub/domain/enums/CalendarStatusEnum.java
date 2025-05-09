package com.formos.huub.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.formos.huub.framework.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CalendarStatusEnum   implements CodeEnum {

    ACTIVE("ACTIVE", "ACTIVE"),
    DISCONNECT("DISCONNECT", "DISCONNECT"),
    ERROR("ERROR", "ERROR");

    private final String value;

    private final String name;

    @JsonValue
    public String getValue() {
        return value;
    }
}
