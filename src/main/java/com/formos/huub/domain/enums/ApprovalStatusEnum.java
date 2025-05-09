package com.formos.huub.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.formos.huub.framework.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ApprovalStatusEnum implements CodeEnum {

    SUBMITTED("SUBMITTED", "SUBMITTED"),
    VENDOR_ASSIGNED("VENDOR_ASSIGNED", "VENDOR_ASSIGNED"),
    APPROVED("APPROVED", "APPROVED"),
    DENIED("DENIED", "DENIED"),
    EXPIRED("EXPIRED", "EXPIRED");

    private final String value;

    private final String name;

    @JsonValue
    public String getValue() {
        return value;
    }
}
