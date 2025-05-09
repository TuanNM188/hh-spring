package com.formos.huub.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.formos.huub.framework.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ActionTypeEnum implements CodeEnum {

    PAGE_VIEW("PAGE_VIEW", "Page View"),
    FAVORITE("FAVORITE", "Favorite"),
    UN_FAVORITE("UN_FAVORITE", "Un Favorite"),
    MARK_SUBMITTED("MARK_SUBMITTED", "Mark Submit"),
    UN_MARK_SUBMITTED("UN_MARK_SUBMITTED", "Un Mark Submit"),
    APPLY_CLICK("APPLY_CLICK", "Apply Click");

    private final String value;

    private final String name;

    @JsonValue
    public String getValue() {
        return value;
    }
}
