package com.formos.huub.domain.response.answerform;

import com.formos.huub.domain.enums.QuestionTypeEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class ResponseViewAnswer {

    private UUID questionId;

    private String questionCode;

    private QuestionTypeEnum questionType;

    private String question;

    private String answer;

    private Integer priorityOrder;

    private Instant answerDate;
}
