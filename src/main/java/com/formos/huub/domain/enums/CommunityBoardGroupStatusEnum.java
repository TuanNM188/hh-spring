package com.formos.huub.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.formos.huub.framework.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CommunityBoardGroupStatusEnum implements CodeEnum {
    JOINED("JOINED", "Joined"),
    SEND_INVITE("SEND_INVITE", "SendInvite"),
    REQUEST_JOIN("REQUEST_JOIN", "RequestJoin");

    private final String value;

    private final String name;

    @JsonValue
    public String getValue() {
        return value;
    }
}
