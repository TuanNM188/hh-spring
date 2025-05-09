package com.formos.huub.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.formos.huub.framework.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum UserStatusEnum implements CodeEnum {
    ACTIVE("ACTIVE", "ACTIVE"),
    INACTIVE("INACTIVE", "INACTIVE"),
    PENDING_APPROVAL("PENDING_APPROVAL", "PENDING_APPROVAL"),
    INVITED("INVITED", "Invited");

    private final String value;

    private final String name;

    @JsonValue
    public String getValue() {
        return value;
    }
}
