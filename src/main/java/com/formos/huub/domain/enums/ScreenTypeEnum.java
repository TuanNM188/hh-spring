package com.formos.huub.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.formos.huub.framework.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ScreenTypeEnum implements CodeEnum {

    MEMBERS("MEMBERS", "Members"),
    ALL_PORTALS("ALL_PORTALS", "ALL_PORTALS"),
    FUNDING_OPPORTUNITIES("FUNDING_OPPORTUNITIES", "Funding Opportunities");

    private final String value;

    private final String name;

    @JsonValue
    public String getValue() {
        return value;
    }
}
