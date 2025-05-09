package com.formos.huub.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.formos.huub.framework.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum QuestionTypeEnum  implements CodeEnum {

    DROPDOWN_MULTIPLE_CHOICE("DROPDOWN_MULTIPLE_CHOICE", "DROPDOWN_MULTIPLE_CHOICE"),
    TEXT("TEXT", "TEXT"),
    TOGGLE_TEXT("TOGGLE_TEXT", "TOGGLE_TEXT"),
    DROPDOWN_SINGLE_CHOICE("DROPDOWN_SINGLE_CHOICE", "DROPDOWN_SINGLE_CHOICE"),
    CHECKBOX("CHECKBOX", "CHECKBOX"),
    RADIOBUTTON("RADIOBUTTON", "RADIOBUTTON"),
    DATE_PICKER("DATE_PICKER", "DATE_PICKER"),
    INPUT_NUMBER("INPUT_NUMBER", "INPUT_NUMBER"),
    RATING("RATING", "RATING"),
    DROPDOWN_MULTIPLE_CHOICE_OTHER("DROPDOWN_MULTIPLE_CHOICE_OTHER", "DROPDOWN_MULTIPLE_CHOICE_OTHER"),
    DROPDOWN_SINGLE_CHOICE_OTHER("DROPDOWN_SINGLE_CHOICE_OTHER", "DROPDOWN_SINGLE_CHOICE_OTHER"),
    TEXT_AREA("TEXT_AREA", "TEXT_AREA"),
    RICH_TEXT("RICH_TEXT", "RICH_TEXT");

    private final String value;

    private final String name;

    @JsonValue
    public String getValue() {
        return value;
    }
}
