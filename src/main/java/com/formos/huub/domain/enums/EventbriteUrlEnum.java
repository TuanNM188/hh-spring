package com.formos.huub.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.formos.huub.framework.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum EventbriteUrlEnum implements CodeEnum {

    SINGLE_EVENT("SINGLE_EVENT", "Single event"),
    ORGANIZER("ORGANIZER", "Organizer"),
    INVALID("INVALID", "Invalid"),
    ORGANIZATION("ORGANIZATION", "Organization");

    private final String value;

    private final String name;

    @JsonValue
    public String getValue() {
        return value;
    }
}
