package com.formos.huub.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.formos.huub.framework.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MeetingPlatformEnum implements CodeEnum {

    ZOOM_VIDEO("ZOOM_VIDEO", "Zoom Video "),
    GOOGLE_MEET("GOOGLE_MEET", "Google Meet"),
    MICROSOFT_TEAMS("MICROSOFT_TEAMS", "Microsoft Teams"),
    OTHER("OTHER", "Other");

    private final String value;

    private final String name;

    @JsonValue
    public String getValue() {
        return value;
    }
}
