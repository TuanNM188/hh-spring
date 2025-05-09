package com.formos.huub.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.formos.huub.framework.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RoleEnum implements CodeEnum {
    ROLE_SYSTEM_ADMINISTRATOR("ROLE_SYSTEM_ADMINISTRATOR", "Admin"),
    ROLE_PORTAL_HOST("ROLE_PORTAL_HOST", "Portal Host"),
    ROLE_BUSINESS_OWNER("ROLE_BUSINESS_OWNER", "Business Owner"),
    ROLE_COMMUNITY_PARTNER("ROLE_COMMUNITY_PARTNER", "Community Partner"),
    ROLE_TECHNICAL_ADVISOR("ROLE_TECHNICAL_ADVISOR", "Technical Advisor");

    private final String value;

    private final String name;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static RoleEnum fromRoleCode(String roleCode) {
        for (RoleEnum role : RoleEnum.values()) {
            if (role.getValue().equals(roleCode)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid role code: " + roleCode);
    }
}
