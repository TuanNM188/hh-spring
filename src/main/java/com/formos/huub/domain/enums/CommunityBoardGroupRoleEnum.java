package com.formos.huub.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.formos.huub.framework.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CommunityBoardGroupRoleEnum implements CodeEnum {
    ORGANIZER("ORGANIZER", "Organizer"),
    MODERATOR("MODERATOR", "Moderator"),
    MEMBER("MEMBER", "Member");

    private final String value;

    private final String name;

    @JsonValue
    public String getValue() {
        return value;
    }
}
