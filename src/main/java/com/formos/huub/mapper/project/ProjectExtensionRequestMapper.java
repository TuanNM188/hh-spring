package com.formos.huub.mapper.project;

import com.formos.huub.domain.entity.ProjectExtensionRequest;
import com.formos.huub.domain.enums.ProjectExtensionRequestStatus;
import com.formos.huub.domain.request.project.RequestCreateProjectExtension;
import com.formos.huub.domain.response.project.ResponseProjectExtensionRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    imports = ProjectExtensionRequestStatus.class
)
public interface ProjectExtensionRequestMapper {
    @Mapping(source = "project.id", target = "projectId")
    ResponseProjectExtensionRequest toResponse(ProjectExtensionRequest entity);

    @Mapping(source = "projectId", target = "project.id")
    @Mapping(target = "status", expression = "java(getDefaultValueStatus())")
    ProjectExtensionRequest toEntity(RequestCreateProjectExtension request);

    default ProjectExtensionRequestStatus getDefaultValueStatus() {
        return ProjectExtensionRequestStatus.PROPOSED;
    }
}
