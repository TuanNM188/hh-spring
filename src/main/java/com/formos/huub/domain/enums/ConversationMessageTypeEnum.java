package com.formos.huub.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.formos.huub.framework.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ConversationMessageTypeEnum implements CodeEnum {

    SYSTEM_MESSAGE("SYSTEM_MESSAGE", "SYSTEM_MESSAGE"),
    DIRECT_MESSAGE("DIRECT_MESSAGE", "DIRECT_MESSAGE");

    private final String value;

    private final String name;

    @JsonValue
    public String getValue() {
        return value;
    }
}
