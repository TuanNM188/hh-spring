package com.formos.huub.domain.request.useranswerform;

import com.formos.huub.framework.validation.constraints.RequireCheck;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RequestAnswerDemoGraphics {

    @RequireCheck
    private String technicalAdvisorId;

    private @Valid List<RequestAnswerForm> answerForms;

}
