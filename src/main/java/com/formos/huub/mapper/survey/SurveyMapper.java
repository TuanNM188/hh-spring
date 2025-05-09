package com.formos.huub.mapper.survey;

import com.formos.huub.domain.entity.Survey;
import com.formos.huub.domain.request.survey.RequestCreateSurvey;
import com.formos.huub.domain.request.survey.RequestUpdateSurvey;
import com.formos.huub.domain.response.survey.ResponseSurvey;
import java.util.UUID;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SurveyMapper {
    @Mapping(target = "name", source = "request.name")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "portal.id", source = "portalId")
    @Mapping(target = "isActive", source = "request.isActive", defaultValue = "true")
    @Mapping(target = "surveyJson", source = "request.surveyJson")
    Survey toEntity(RequestCreateSurvey request, UUID portalId);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "request.name")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "surveyJson", source = "request.surveyJson")
    Survey partialUpdate(@MappingTarget Survey survey, RequestUpdateSurvey request);

    @Mapping(target = "portalId", source = "portal.id")
    @Mapping(target = "portalUrl", source = "portal.url")
    @Mapping(target = "isCustomDomain", source = "portal.isCustomDomain")
    ResponseSurvey toResponse(Survey survey);
}
