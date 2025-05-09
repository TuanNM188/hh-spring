package com.formos.huub.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.formos.huub.framework.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DaysOfWeek implements CodeEnum {

    SUNDAY("SUNDAY", "Sunday"),
    MONDAY("MONDAY", "Monday"),
    TUESDAY("TUESDAY", "Tuesday"),
    WEDNESDAY("WEDNESDAY", "Wednesday"),
    THURSDAY("THURSDAY", "Thursday"),
    FRIDAY("FRIDAY", "Friday"),
    SATURDAY("SATURDAY", "Saturday");

    private final String value;

    private final String name;

    @JsonValue
    public String getValue() {
        return value;
    }
}
