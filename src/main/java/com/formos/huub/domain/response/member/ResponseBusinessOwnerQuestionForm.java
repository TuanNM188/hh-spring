package com.formos.huub.domain.response.member;

import com.formos.huub.domain.response.answerform.ResponseQuestionForm;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ResponseBusinessOwnerQuestionForm {

    private List<ResponseQuestionForm> businessDetails;

    private List<ResponseQuestionForm> demographics;

    private List<ResponseQuestionForm> assistanceNeeds;

    private List<ResponseQuestionForm> portalQuestions;

}
