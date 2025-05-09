package com.formos.huub.domain.response.portals;

import com.formos.huub.domain.response.answerform.ResponseQuestionForm;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ResponsePortalIntakeQuestion {

    private List<ResponseQuestionForm> intakeQuestionProfile;

    private List<ResponseQuestionForm> intakeQuestionDemographic;

    private List<ResponseQuestionForm> intakeQuestionBusiness;

    private List<ResponseQuestionForm> intakeAdditionalQuestions;

    private List<ResponseQuestionForm> intakeTechnicalAssistanceAdditionalQuestions;

    private List<ResponseQuestionForm> intakeQuestionAssistanceNeed;

    private List<ResponseQuestionForm> intakeQuestionSupportApplication;

}
