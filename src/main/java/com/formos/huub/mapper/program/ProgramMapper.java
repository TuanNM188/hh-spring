package com.formos.huub.mapper.program;

import com.formos.huub.domain.entity.Program;
import com.formos.huub.domain.request.portals.RequestPortalProgram;
import com.formos.huub.domain.response.portals.ResponsePortalProgram;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {ProgramTermMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProgramMapper {

    Program toEntity(RequestPortalProgram request);

    void partialEntity(@MappingTarget Program program, RequestPortalProgram request);

    ResponsePortalProgram toResponse(Program program);
}
