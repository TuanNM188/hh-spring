package com.formos.huub.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.formos.huub.framework.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum EventRegistrationStatusEnum implements CodeEnum {

    CHECKED_IN("CHECKED_IN", "Checked In"),
    NOT_ATTENDING("NOT_ATTENDING", "Not Attending"),
    GUESTS_ATTENDED("GUESTS_ATTENDED", "Guests Attended"),
    GUESTS_ATTENDING("GUESTS_ATTENDING", "Guests Attending"),
    ATTENDING("ATTENDING", "Attending");

    private final String value;

    private final String name;

    @JsonValue
    public String getValue() {
        return value;
    }
}
