package com.formos.huub.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.formos.huub.framework.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum EntityTypeEnum implements CodeEnum {

    APPOINTMENT_REPORT("APPOINTMENT_REPORT", "Appointment Report"),
    PROJECT_REPORT("PROJECT_REPORT", "Project Report"),
    PROJECT_UPDATE("PROJECT_UPDATE", "Project Update"),
    PROJECT("PROJECT", "Project"),
    CLIENT_NOTE("CLIENT_NOTE", "Client Note");

    private final String value;

    private final String name;

    @JsonValue
    public String getValue() {
        return value;
    }
}
