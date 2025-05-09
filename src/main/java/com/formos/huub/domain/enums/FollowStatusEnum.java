package com.formos.huub.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.formos.huub.framework.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum FollowStatusEnum implements CodeEnum {
    CONNECTED("CONNECTED", "Connected"),
    DISCONNECTED("DISCONNECTED", "Disconnected"),
    PENDING("PENDING", "Pending");

    private final String value;

    private final String name;

    @JsonValue
    public String getValue() {
        return value;
    }
}
