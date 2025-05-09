package com.formos.huub.mapper.useranswerform;

import com.formos.huub.domain.entity.AnswerOption;
import com.formos.huub.domain.request.useranswerform.RequestAnswerOption;
import com.formos.huub.domain.response.answerform.ResponseAnswer;
import com.formos.huub.domain.response.answerform.ResponseQuestionForm;
import org.mapstruct.*;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface AnswerOptionMapper {

    @Mapping(target = "questionId", source = "question.id")
    ResponseAnswer toResponse(AnswerOption answerOption);

    AnswerOption toEntity(RequestAnswerOption requestAnswerOption);

    void partialEntity(@MappingTarget AnswerOption answerOption, RequestAnswerOption requestAnswerOption);
}
