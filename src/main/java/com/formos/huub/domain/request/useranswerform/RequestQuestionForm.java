package com.formos.huub.domain.request.useranswerform;

import com.formos.huub.domain.enums.FormCodeEnum;
import com.formos.huub.domain.enums.GroupCodeEnum;
import com.formos.huub.domain.enums.OptionTypeEnum;
import com.formos.huub.domain.enums.QuestionTypeEnum;
import com.formos.huub.domain.response.answerform.ResponseAnswer;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RequestQuestionForm {

    private UUID id;

    private String question;

    private QuestionTypeEnum questionType;

    private GroupCodeEnum groupCode;

    private String questionCode;

    private Integer priorityOrder;

    private Boolean isRequire;

    private FormCodeEnum formCode;

    private UUID parentId;

    private List<ResponseAnswer> answers;

    private Boolean allowCustomOptions;

    private String columnSize;

    private String isVisible;

    private UUID portalIntakeQuestionId;

    private Boolean allowOtherInput;

    private String placeholder;

    private OptionTypeEnum optionType;

    private Boolean allowActionVisible;

    private String description;

}
