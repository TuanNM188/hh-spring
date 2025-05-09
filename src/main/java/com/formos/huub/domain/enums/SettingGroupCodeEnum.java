package com.formos.huub.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.formos.huub.framework.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SettingGroupCodeEnum implements CodeEnum {

    GENERAL("GENERAL", "GENERAL"),
    MENTIONS("MENTIONS", "MENTIONS"),
    ACCOUNT_SETTINGS("ACCOUNT_SETTINGS", "ACCOUNT_SETTINGS"),
    COMMUNITY_BOARD("COMMUNITY_BOARD", "COMMUNITY_BOARD"),
    GROUPS("GROUPS", "GROUPS"),
    PRIVATE_MESSAGES("PRIVATE_MESSAGES", "PRIVATE_MESSAGES"),
    MEMBER_CONNECTIONS("MEMBER_CONNECTIONS", "MEMBER_CONNECTIONS"),
    ABOUT("ABOUT", "ABOUT"),
    DETAILS("DETAILS", "DETAILS"),
    COMMUNITY_BOARD_GROUP("COMMUNITY_BOARD_GROUP", "COMMUNITY_BOARD_GROUP");

    private final String value;

    private final String name;

    @JsonValue
    public String getValue() {
        return value;
    }
}
