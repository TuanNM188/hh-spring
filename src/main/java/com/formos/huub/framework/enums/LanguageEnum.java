package com.formos.huub.framework.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum LanguageEnum implements CodeEnum {
    EN("en", "English"),
    ES("es", "Spanish"),
    AUTO("auto", "Auto");

    private final String value;
    private final String name;

    @JsonCreator
    public static LanguageEnum fromCode(String code) {
        for (LanguageEnum lang : LanguageEnum.values()) {
            if (lang.getValue().equalsIgnoreCase(code)) {
                return lang;
            }
        }
        throw new IllegalArgumentException("Unknown language code: " + code);
    }
}
