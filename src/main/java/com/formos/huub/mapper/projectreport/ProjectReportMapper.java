package com.formos.huub.mapper.projectreport;

import com.formos.huub.domain.entity.ProjectReport;
import com.formos.huub.domain.request.projectreport.RequestCreateProjectReport;
import java.util.UUID;

import com.formos.huub.domain.request.projectreport.RequestUpdateProjectReport;
import com.formos.huub.domain.response.project.ResponseProjectReport;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProjectReportMapper {
    @Mapping(target = "feedback", source = "request.feedback")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "project.id", source = "projectId")
    @Mapping(
        target = "serviceOutcomes",
        expression = "java(com.formos.huub.framework.utils.StringUtils.convertListToString(request.getServiceOutcomes()))"
    )
    @Mapping(target = "hoursCompleted", source = "request.hoursCompleted")
    @Mapping(target = "confirmation", source = "request.confirmation")
    ProjectReport toEntity(RequestCreateProjectReport request, UUID projectId);

    @Mapping(target = "feedback", source = "request.feedback")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "project.id", source = "projectId")
    @Mapping(
        target = "serviceOutcomes",
        expression = "java(com.formos.huub.framework.utils.StringUtils.convertListToString(request.getServiceOutcomes()))"
    )
    @Mapping(target = "hoursCompleted", source = "request.hoursCompleted")
    @Mapping(target = "confirmation", source = "request.confirmation")
    ProjectReport toEntity(RequestUpdateProjectReport request, UUID projectId);

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(
        target = "serviceOutcomes",
        expression = "java(com.formos.huub.framework.utils.StringUtils.convertStringToListString(entity.getServiceOutcomes()))"
    )
    ResponseProjectReport toResponse(ProjectReport entity);
}
