package com.formos.huub.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.formos.huub.framework.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RegistrationStatusEnum implements CodeEnum {

    SUBMITTED("SUBMITTED", "SUBMITTED"),
    APPROVED("APPROVED", "APPROVED"),
    DENIED("DENIED", "DENIED");

    private final String value;

    private final String name;

    @JsonValue
    public String getValue() {
        return value;
    }
}
