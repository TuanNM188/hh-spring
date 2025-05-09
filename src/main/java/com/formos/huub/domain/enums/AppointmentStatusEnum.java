package com.formos.huub.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AppointmentStatusEnum {
    SCHEDULED ("SCHEDULED", "SCHEDULED"),
    REPORT_REQUIRED("REPORT_REQUIRED", "REPORT_REQUIRED"),
    OVERDUE("OVERDUE", "OVERDUE"),
    COMPLETED("COMPLETED", "COMPLETED"),
    INVOICED("INVOICED", "INVOICED"),
    CANCELED("CANCELED", "CANCELED");

    private final String value;

    private final String name;

    @JsonValue
    public String getValue() {
        return value;
    }
}
