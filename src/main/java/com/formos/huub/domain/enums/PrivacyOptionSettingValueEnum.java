package com.formos.huub.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.formos.huub.framework.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PrivacyOptionSettingValueEnum implements CodeEnum {

    PUBLIC_GROUPS("PUBLIC_GROUPS", "PUBLIC_GROUPS"),
    PRIVATE_GROUPS("PRIVATE_GROUPS", "PRIVATE_GROUPS"),
    HIDDEN_GROUPS("HIDDEN_GROUPS", "HIDDEN_GROUPS");

    private final String value;

    private final String name;

    @JsonValue
    public String getValue() {
        return value;
    }
}
