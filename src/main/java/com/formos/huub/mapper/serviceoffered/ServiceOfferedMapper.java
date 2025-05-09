package com.formos.huub.mapper.serviceoffered;

import com.formos.huub.domain.entity.ServiceOffered;
import com.formos.huub.domain.response.serviceoffered.ResponseServiceOffered;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ServiceOfferedMapper {
    ResponseServiceOffered toResponse(ServiceOffered serviceOffered);

    List<ResponseServiceOffered> toListResponse(List<ServiceOffered> serviceOffereds);
}
