package com.formos.huub.mapper.specialty;

import com.formos.huub.domain.entity.SpecialtyArea;
import com.formos.huub.domain.request.specialty.RequestCreateSpecialtyArea;
import com.formos.huub.domain.response.specialty.ResponseSpecialtyArea;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SpecialtyAreaMapper {

    ResponseSpecialtyArea toResponse(SpecialtyArea specialtyArea);

    SpecialtyArea toEntity(RequestCreateSpecialtyArea request);

    List<ResponseSpecialtyArea> toListResponse(List<SpecialtyArea> specialtyAreas);
}
