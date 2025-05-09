package com.formos.huub.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.formos.huub.framework.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CommunityBoardActionTypeEnum implements CodeEnum {
    RESTRICT_POST("RESTRICT_POST", "RESTRICT_POST"),
    DELETE_COMMENT("DELETE_COMMENT", "DELETE_COMMENT"),
    DELETE_POST("DELETE_POST", "DELETE_POST"),
    UPDATE_POST("UPDATE_POST", "UPDATE_POST"),
    UPDATE_COMMENT("UPDATE_COMMENT", "UPDATE_COMMENT"),
    FLAG_POST("FLAG_POST", "FLAG_POST"),
    UNRESTRICT_POST("UNRESTRICT_POST", "UNRESTRICT_POST"),
    ;

    private final String value;

    private final String name;

    @JsonValue
    public String getValue() {
        return value;
    }
}
