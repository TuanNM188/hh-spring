package com.formos.huub.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.formos.huub.framework.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ***************************************************
 * * Description :
 * * File        : SocialProviderEnum
 * * Author      : Hung Tran
 * * Date        : Oct 28, 2024
 * ***************************************************
 **/

@AllArgsConstructor
@Getter
public enum SocialProviderEnum implements CodeEnum {
    GOOGLE("GOOGLE", "GOOGLE"),
    FACEBOOK("FACEBOOK", "FACEBOOK"),
    MICROSOFT("MICROSOFT", "MICROSOFT");

    private final String value;

    private final String name;

    @JsonValue
    public String getValue() {
        return value;
    }
}
