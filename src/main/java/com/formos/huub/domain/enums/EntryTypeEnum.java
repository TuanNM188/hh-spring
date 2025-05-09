package com.formos.huub.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.formos.huub.framework.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum EntryTypeEnum  implements CodeEnum {

    USER("USER", "USER"),
    PORTAL_PROGRAM("PORTAL_PROGRAM", "PORTAL_PROGRAM"),
    PORTAL("PORTAL", "PORTAL");
    private final String value;

    private final String name;

    @JsonValue
    public String getValue() {
        return value;
    }
}
