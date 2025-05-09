package com.formos.huub.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.formos.huub.framework.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum FilterStatusEnum implements CodeEnum {

    EXPIRING_SOON("EXPIRING_SOON", "Expiring Soon"),
    RECENTLY_ADDED("RECENTLY_ADDED", "Recently Added"),
    EXPIRED("EXPIRED", "Expired");

    private final String value;

    private final String name;

    @JsonValue
    public String getValue() {
        return value;
    }
}
