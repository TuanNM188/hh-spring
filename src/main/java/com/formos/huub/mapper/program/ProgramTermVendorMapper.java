package com.formos.huub.mapper.program;

import com.formos.huub.domain.entity.ProgramTermVendor;
import com.formos.huub.domain.request.portals.RequestProgramTermVendor;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProgramTermVendorMapper {

    ProgramTermVendor toEntity(RequestProgramTermVendor request);

    void partialEntity(@MappingTarget ProgramTermVendor programTermVendor, RequestProgramTermVendor request);
}
