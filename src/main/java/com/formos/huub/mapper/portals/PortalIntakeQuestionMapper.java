package com.formos.huub.mapper.portals;

import com.formos.huub.domain.entity.PortalIntakeQuestion;
import com.formos.huub.domain.entity.Question;
import com.formos.huub.domain.request.useranswerform.RequestAnswerForm;
import com.formos.huub.domain.request.useranswerform.RequestQuestionForm;
import com.formos.huub.domain.response.answerform.ResponsePortalAnswerForm;
import org.mapstruct.*;

import java.util.UUID;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PortalIntakeQuestionMapper {

    @Mapping(target = "id", source = "request.portalIntakeQuestionId")
    @Mapping(target = "portalId", source = "portalId")
    @Mapping(target = "questionId", source = "request.id")
    @Mapping(target = "isVisible", source = "request.isVisible")
    @Mapping(target = "priorityOrder", source = "request.priorityOrder")
    @Mapping(target = "columnSize", source = "request.columnSize")
    PortalIntakeQuestion toEntity(RequestQuestionForm request, UUID portalId);

    @Mapping(target = "id",ignore = true)
    void partialEntity(@MappingTarget PortalIntakeQuestion portalIntakeQuestion, RequestQuestionForm request);

    @Mapping(target = "portalId", source = "portalId")
    @Mapping(target = "questionId", source = "question.id")
    @Mapping(target = "isVisible", expression = "java(true)")
    @Mapping(target = "allowOtherInput", expression = "java(false)")
    PortalIntakeQuestion toEntityFromQuestion(Question question, UUID portalId);

    ResponsePortalAnswerForm toResponse(PortalIntakeQuestion portalIntakeQuestion);

}
