package com.formos.huub.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.formos.huub.framework.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CommunityBoardVisibilityEnum implements CodeEnum {
    ALL_MEMBERS("ALL_MEMBERS", "All Members"),
    MY_CONNECTIONS("MY_CONNECTIONS", "My Connections"),
    GROUP("GROUP", "Group"),
    ONLY_ME("ONLY_ME", "Only me");

    private final String value;

    private final String name;

    @JsonValue
    public String getValue() {
        return value;
    }
}
