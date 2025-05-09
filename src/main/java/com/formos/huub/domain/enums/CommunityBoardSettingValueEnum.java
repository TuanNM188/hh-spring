package com.formos.huub.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.formos.huub.framework.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CommunityBoardSettingValueEnum implements CodeEnum {

    ALL_GROUP_MEMBERS("ALL_GROUP_MEMBERS", "ALL_GROUP_MEMBERS"),
    ORGANIZERS_AND_MODERATORS_ONLY("ORGANIZERS_AND_MODERATORS_ONLY", "ORGANIZERS_AND_MODERATORS_ONLY"),
    ORGANIZERS_ONLY("ORGANIZERS_ONLY", "ORGANIZERS_ONLY");

    private final String value;

    private final String name;

    @JsonValue
    public String getValue() {
        return value;
    }
}
