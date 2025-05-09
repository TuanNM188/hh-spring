package com.formos.huub.domain.request.portals;

import com.formos.huub.domain.request.useranswerform.RequestAdditionalQuestion;
import com.formos.huub.domain.request.useranswerform.RequestQuestionForm;
import com.formos.huub.framework.validation.constraints.RequireCheck;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RequestPortalIntakeQuestion {

    @RequireCheck
    private String portalId;

    private @Valid List<RequestQuestionForm> intakeQuestionProfile;

    private @Valid List<RequestQuestionForm> intakeQuestionDemographic;

    private @Valid List<RequestAdditionalQuestion> intakeAdditionalQuestions;

    private @Valid List<RequestQuestionForm> intakeQuestionAssistanceNeed;

    private @Valid List<RequestQuestionForm> intakeQuestionSupportApplication;

    private @Valid List<RequestAdditionalQuestion> intakeTechnicalAssistanceAdditionalQuestions;


}
