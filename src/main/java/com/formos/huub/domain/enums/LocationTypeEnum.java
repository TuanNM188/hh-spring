package com.formos.huub.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.formos.huub.framework.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum LocationTypeEnum implements CodeEnum {

    COUNTRY("COUNTRY", "COUNTRY"),
    STATE("STATE", "STATE"),
    ZIPCODE("ZIPCODE", "ZIPCODE"),
    CITY("CITY", "CITY");

    private final String value;

    private final String name;

    @JsonValue
    public String getValue() {
        return value;
    }
}
