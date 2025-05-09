package com.formos.huub.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.formos.huub.framework.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CommunityBoardTargetTypeEnum implements CodeEnum {
    USER("USER", "USER"),
    COMMENT("COMMENT", "COMMENT"),
    POST("POST", "POST"),
    ;

    private final String value;

    private final String name;

    @JsonValue
    public String getValue() {
        return value;
    }
}
