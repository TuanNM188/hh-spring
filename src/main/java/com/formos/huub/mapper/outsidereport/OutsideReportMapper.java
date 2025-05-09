package com.formos.huub.mapper.outsidereport;

import com.formos.huub.domain.entity.OutsideReport;
import com.formos.huub.domain.request.outsidereport.RequestCreateOutsideReport;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OutsideReportMapper {

    @Mapping(target = "portal.id", source = "portalId")
    OutsideReport toEntity(RequestCreateOutsideReport request, UUID portalId);
}
