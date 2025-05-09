package com.formos.huub.mapper.specialty;

import com.formos.huub.domain.entity.Specialty;
import com.formos.huub.domain.request.specialty.RequestCreateSpecialty;
import com.formos.huub.domain.request.specialty.RequestUpdateSpecialty;
import com.formos.huub.domain.response.categories.IResponseCategory;
import com.formos.huub.domain.response.categories.ResponseCategories;
import com.formos.huub.domain.response.specialty.ResponseSpecialty;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SpecialtyMapper {

    ResponseSpecialty toResponse(Specialty specialty);

    List<ResponseSpecialty> toListResponse(List<Specialty> specialties);

    Specialty toEntity(RequestCreateSpecialty request);

    List<ResponseCategories> toListResponseFromInterface(List<IResponseCategory> categories);

    void partialEntity(@MappingTarget Specialty specialty, RequestUpdateSpecialty request);
}
