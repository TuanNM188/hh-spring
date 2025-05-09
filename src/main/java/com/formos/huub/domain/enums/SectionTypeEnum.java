package com.formos.huub.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.formos.huub.framework.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SectionTypeEnum implements CodeEnum {

    IMAGE("IMAGE", "Image"),
    TEXT("TEXT", "Text"),
    AUDIO("AUDIO", "Audio"),
    VIDEO("VIDEO", "Video"),
    FILES("FILES", "Files"),
    SURVEY("SURVEY", "Survey");

    private final String value;

    private final String name;

    @JsonValue
    public String getValue() {
        return value;
    }
}
