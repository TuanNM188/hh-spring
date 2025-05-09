package com.formos.huub.domain.request.useranswerform;

import com.formos.huub.domain.enums.OptionTypeEnum;
import com.formos.huub.framework.validation.constraints.RequireCheck;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RequestAnswerForm {

    private UUID questionId;

    private String questionCode;

    private String additionalAnswer;

    private Boolean isActive;

    private List<String> answerOptions;

    private OptionTypeEnum optionType;

    private Boolean isVisible;

    private UUID entryFormId;
}
