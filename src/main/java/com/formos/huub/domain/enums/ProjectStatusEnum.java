package com.formos.huub.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.formos.huub.framework.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ProjectStatusEnum implements CodeEnum {

    PROPOSED("PROPOSED", "PROPOSED"),
    VENDOR_APPROVED("VENDOR_APPROVED", "VENDOR_APPROVED"),
    WORK_IN_PROGRESS("WORK_IN_PROGRESS", "WORK_IN_PROGRESS"),
    COMPLETED("COMPLETED", "COMPLETED"),
    OVERDUE("OVERDUE", "OVERDUE"),
    INVOICED("INVOICED", "INVOICED"),
    DENIED("DENIED", "DENIED");

    private final String value;

    private final String name;

    @JsonValue
    public String getValue() {
        return value;
    }
}
