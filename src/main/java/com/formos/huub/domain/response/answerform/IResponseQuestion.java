package com.formos.huub.domain.response.answerform;

import com.formos.huub.domain.enums.*;

import java.util.UUID;

public interface IResponseQuestion {

    UUID getId();

    String getQuestion();

    QuestionTypeEnum getQuestionType();

    String getQuestionCode();

    GroupCodeEnum getGroupCode();

    Integer getPriorityOrder();

    Boolean getIsRequire();

    FormCodeEnum getFormCode();

    UUID getParentId();

    OptionTypeEnum getOptionType();

    Boolean getAllowCustomOptions();

    String getColumnSize();

    String getIsVisible();

    UUID getPortalIntakeQuestionId();

    Boolean getAllowOtherInput();

    String getPlaceholder();

    Boolean getAllowActionVisible();

    String getDescription();

    InputTypeEnum getInputType();

    String getMessageValidate();

}
