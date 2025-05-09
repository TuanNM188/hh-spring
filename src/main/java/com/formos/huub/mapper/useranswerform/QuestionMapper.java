package com.formos.huub.mapper.useranswerform;

import com.formos.huub.domain.entity.Question;
import com.formos.huub.domain.enums.FormCodeEnum;
import com.formos.huub.domain.request.useranswerform.RequestAdditionalQuestion;
import com.formos.huub.domain.response.answerform.IResponseQuestion;
import com.formos.huub.domain.response.answerform.ResponseQuestionForm;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface QuestionMapper {
    ResponseQuestionForm toResponse(Question question);

    ResponseQuestionForm toResponseFromInterface(IResponseQuestion question);

    Question toEntity(IResponseQuestion question);

    Question toEntityAdditional(RequestAdditionalQuestion requestAdditionalQuestion, FormCodeEnum formCode, String questionCode, String columnSize);

    void partialEntityAdditional(@MappingTarget Question question, RequestAdditionalQuestion request);
}
