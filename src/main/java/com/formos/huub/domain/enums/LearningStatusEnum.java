package com.formos.huub.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.formos.huub.framework.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum LearningStatusEnum implements CodeEnum {

    NOT_STARTED("NOT_STARTED", "NOT_STARTED"),
    STARTED("STARTED", "STARTED"),
    COMPLETE("COMPLETE", "COMPLETE");

    private final String value;

    private final String name;

    @JsonValue
    public String getValue() {
        return value;
    }
}
