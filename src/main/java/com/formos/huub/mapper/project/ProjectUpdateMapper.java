package com.formos.huub.mapper.project;

import com.formos.huub.domain.entity.ProjectUpdate;
import com.formos.huub.domain.request.project.RequestCreateProjectUpdate;
import com.formos.huub.domain.response.project.ResponseProjectUpdate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProjectUpdateMapper {

    @Mapping(target = "projectId", source = "entity.project.id")
    @Mapping(target = "description", source = "entity.description")
    @Mapping(target = "imageUrl", source = "imageUrl")
    @Mapping(target = "createdDate", source = "entity.createdDate")
    @Mapping(target = "createdBy", source = "createdBy")
    ResponseProjectUpdate toResponse(ProjectUpdate entity, String imageUrl, String createdBy);

    @Mapping(target = "project.id", source = "projectId")
    @Mapping(target = "description", source = "description")
    ProjectUpdate toEntity(RequestCreateProjectUpdate request);

}
