package com.formos.huub.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.formos.huub.framework.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ConditionTypeEnum implements CodeEnum {
    EQUAL("equals", "CAST(%s AS text) = %s",""),
    CONTAIN("contains", "lower(%s) LIKE lower(%s)",""),
    ENDS_WITH("endsWith", "lower(%s) like lower(%s)",""),
    BEGINS_WITH("startsWith", "lower(%s) like lower(%s)",""),
    DOES_NOT_CONTAIN("notContains", "lower(%s) not like lower(%s)",""),
    DOES_NOT_EQUAL("notEquals", "%s != %s",""),
    BLANK("blank", "%s IS NULL",""),
    NOT_BLANK("notBlank", "%s IS NOT NULL",""),
    BEFORE("lessThan", "%s < %s",""),
    LESS_THAN("lt", "%s < %s",""),
    LESS_THAN_OR_EQUAL("lte", "%s <= %s",""),
    DATE_BEFORE("dateBefore", "%s < %s",""),
    AFTER("greaterThan", "%s > %s",""),
    GREATER_THAN("gt", "%s > %s",""),
    GREATER_THAN_OR_EQUAL("gte", "%s >= %s",""),
    DATE_AFTER("dateAfter", "%s > %s",""),
    DATE_IS_NOT("dateIsNot", "%s != %s",""),
    DATE_IS("dateIs", "%s = %s",""),
    BETWEEN("inRange", "%s  BETWEEN %s AND %s","");
    private final String value;
    private final String operator;
    private final String name;

    @JsonValue
    public String getValue() {
        return value;
    }
}
