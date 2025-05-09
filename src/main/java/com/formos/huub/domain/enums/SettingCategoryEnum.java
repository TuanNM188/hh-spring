package com.formos.huub.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.formos.huub.framework.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SettingCategoryEnum implements CodeEnum {

    NOTIFICATION("NOTIFICATION", "NOTIFICATION"),
    PRIVACY("PRIVACY", "PRIVACY"),
    COMMUNITY_BOARD_GROUP("COMMUNITY_BOARD_GROUP", "COMMUNITY_BOARD_GROUP");

    private final String value;

    private final String name;

    @JsonValue
    public String getValue() {
        return value;
    }
}
