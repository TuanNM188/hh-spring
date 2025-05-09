package com.formos.huub.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.formos.huub.framework.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MessageTypeEnum  implements CodeEnum {

    TEXT("TEXT", "TEXT"),
    GIF("GIF", "GIF"),
    MEDIA_IMAGES("MEDIA_IMAGES", "MEDIA_IMAGES");

    private final String value;

    private final String name;

    @JsonValue
    public String getValue() {
        return value;
    }
}
