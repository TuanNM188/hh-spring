package com.formos.huub.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.formos.huub.framework.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TermsAndConditionsTypeEnum implements CodeEnum {

    LEARNING_LIBRARY("LEARNING_LIBRARY", "LEARNING_LIBRARY"),
    TERMS_AND_CONDITIONS("TERMS_AND_CONDITIONS", "TERMS_AND_CONDITIONS"),
    PRIVACY_POLICY("PRIVACY_POLICY", "PRIVACY_POLICY");

    private final String value;

    private final String name;

    @JsonValue
    public String getValue() {
        return value;
    }
}
