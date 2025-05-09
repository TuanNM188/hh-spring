package com.formos.huub.framework.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TimePeriodEnum implements CodeEnum {
    DAILY("DAILY", "Daily"),
    WEEKLY("WEEKLY", "Weekly"),
    MONTHLY("MONTHLY", "Monthly"),
    YEARLY("YEARLY", "Yearly");

    private final String value;

    private final String name;

    @JsonValue
    public String getValue() {
        return value;
    }
}
