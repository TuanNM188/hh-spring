package com.formos.huub.domain.response.answerform;

import com.formos.huub.domain.enums.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ResponseQuestionForm {

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

    private Boolean isVisible;

    private UUID portalIntakeQuestionId;

    private Boolean allowOtherInput;

    private String placeholder;

    private OptionTypeEnum optionType;

    private Boolean allowActionVisible;

    private String description;

    private InputTypeEnum inputType;

    private String messageValidate;

    private String relatedQuestionCode;

}
