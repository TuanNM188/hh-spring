package com.formos.huub.mapper.program;

import com.formos.huub.domain.entity.ProgramTerm;
import com.formos.huub.domain.request.portals.RequestProgramTerm;
import com.formos.huub.domain.response.portals.ResponseProgramTerm;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProgramTermMapper {
    ProgramTerm toEntity(RequestProgramTerm request);

    void partialEntity(@MappingTarget ProgramTerm programTerm, RequestProgramTerm request);

    @Mapping(target = "programManagerId", source = "programManager.id")
    ResponseProgramTerm toResponse(ProgramTerm programTerm);
}
