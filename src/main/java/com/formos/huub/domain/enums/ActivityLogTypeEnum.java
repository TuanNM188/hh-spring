package com.formos.huub.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ActivityLogTypeEnum {
    LOGIN("LOGIN", "Login"),
    LOGIN_AS_USER("LOGIN_AS_USER", "Login as User"),
    LOGOUT("LOGOUT", "Logout"),
    REFRESH_TOKEN("REFRESH_TOKEN", "Refresh Token"),
    ACCESS_TOKEN_EXPIRE("ACCESS_TOKEN_EXPIRE", "Access Token Expire"),
    REFRESH_TOKEN_EXPIRE("REFRESH_TOKEN_EXPIRE", "Refresh Token Expire");

    private final String value;

    private final String name;

    @JsonValue
    public String getValue() {
        return value;
    }
}
