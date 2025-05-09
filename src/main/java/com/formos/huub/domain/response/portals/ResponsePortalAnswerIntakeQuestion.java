package com.formos.huub.domain.response.portals;

import com.formos.huub.domain.response.answerform.ResponsePortalAnswerForm;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ResponsePortalAnswerIntakeQuestion {

    private List<ResponsePortalAnswerForm> intakeQuestionProfile;

    private List<ResponsePortalAnswerForm> intakeQuestionDemographic;

}
