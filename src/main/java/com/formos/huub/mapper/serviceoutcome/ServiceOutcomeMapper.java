package com.formos.huub.mapper.serviceoutcome;

import com.formos.huub.domain.entity.ServiceOutcome;
import com.formos.huub.domain.response.serviceoutcome.ResponseServiceOutcome;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ServiceOutcomeMapper {

    ResponseServiceOutcome toResponse(ServiceOutcome serviceOutcome);

    List<ResponseServiceOutcome> toListResponse(List<ServiceOutcome> serviceOutcomes);
}
