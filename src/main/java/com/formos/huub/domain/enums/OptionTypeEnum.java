package com.formos.huub.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.formos.huub.framework.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum OptionTypeEnum implements CodeEnum {
    ANSWER_OPTION("ANSWER_OPTION", "ANSWER_OPTION"),
    TEXT("TEXT", "TEXT"),
    LANGUAGE("LANGUAGE", "LANGUAGE"),
    CATEGORY("CATEGORY", "CATEGORY"),
    SERVICE("SERVICE", "SERVICE"),
    SERVICE_OUTCOME("SERVICE_OUTCOME", "SERVICE_OUTCOME"),
    COMMUNITY_PARTNER("COMMUNITY_PARTNER", "COMMUNITY_PARTNER"),
    STATE("STATE", "STATE"),
    COUNTRY("COUNTRY", "COUNTRY"),
    CITY("CITY", "CITY");

    private final String value;

    private final String name;

    @JsonValue
    public String getValue() {
        return value;
    }
}
