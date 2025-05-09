package com.formos.huub.domain.request.useranswerform;

import com.formos.huub.domain.enums.QuestionTypeEnum;
import com.formos.huub.framework.validation.constraints.RequireCheck;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RequestAdditionalQuestion {

    private UUID id;

    @RequireCheck
    private String question;

    @NotNull
    private QuestionTypeEnum questionType;

    private List<RequestAnswerOption> answers;
}
