package com.formos.huub.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.formos.huub.framework.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum InputTypeEnum implements CodeEnum {

    TEXT("TEXT", ""),
    URL("URL", ""),
    EMAIL("EMAIL", ""),
    NUMBER("NUMBER", ""),
    NUMBER_PHONE("NUMBER_PHONE", "");

    private final String value;

    private final String name;

    @JsonValue
    public String getValue() {
        return value;
    }
}
