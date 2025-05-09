package com.formos.huub.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.formos.huub.framework.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MenuPositionEnum implements CodeEnum {
    TOP("TOP", ""),
    BOTTOM("BOTTOM", ""),
    CONFIG("CONFIG", ""),
    TA_MANAGEMENT("TA_MANAGEMENT", ""),
    METRICS_REPORTS("METRICS_REPORTS", ""),
    COURSE_MANAGEMENT("COURSE_MANAGEMENT", "");

    private final String value;

    private final String name;

    @JsonValue
    public String getValue() {
        return value;
    }
}
