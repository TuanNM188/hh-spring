package com.formos.huub.mapper.survey;

import com.formos.huub.domain.entity.SurveyResponses;
import com.formos.huub.domain.response.survey.ResponseSurveyResponses;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SurveyResponsesMapper {
    @Mapping(target = "fullName", source = "surveyResponses.user.normalizedFullName")
    @Mapping(target = "role", source = "roleUser")
    ResponseSurveyResponses toResponse(SurveyResponses surveyResponses, String roleUser);
}
