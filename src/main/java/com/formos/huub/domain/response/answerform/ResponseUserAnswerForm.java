package com.formos.huub.domain.response.answerform;

import com.formos.huub.domain.enums.OptionTypeEnum;
import com.formos.huub.domain.enums.QuestionTypeEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ResponseUserAnswerForm {

    private UUID questionId;

    private String questionCode;

    private QuestionTypeEnum questionType;

    private String question;

    private String additionalAnswer;

    private Boolean isActive;

    private List<String> answerOptions;

    private OptionTypeEnum optionType;

    private String answer;

    private Integer priorityOrder;

    private Instant answerDate;

    private UUID parentId;
}
