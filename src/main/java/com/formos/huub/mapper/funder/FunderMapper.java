package com.formos.huub.mapper.funder;

import com.formos.huub.domain.entity.Funder;
import com.formos.huub.domain.request.funder.RequestCreateFunder;
import com.formos.huub.domain.request.funder.RequestUpdateFunder;
import com.formos.huub.domain.response.funder.ResponseFunder;
import com.formos.huub.domain.response.funder.ResponseFunderDetail;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FunderMapper {

    ResponseFunder toResponse(Funder funder);

    List<ResponseFunder> toListResponse(List<Funder> funders);

    Funder toEntity(RequestCreateFunder request);

    Funder partialUpdate(@MappingTarget Funder funder, RequestUpdateFunder request);

    ResponseFunderDetail toResponseDetail(Funder funder);

    Funder stringToEntity(String id);

}
