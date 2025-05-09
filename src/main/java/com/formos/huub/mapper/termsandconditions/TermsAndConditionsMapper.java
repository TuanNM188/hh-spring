package com.formos.huub.mapper.termsandconditions;

import com.formos.huub.domain.entity.TermsAndConditions;
import com.formos.huub.domain.response.termsandconditions.ResponseTermsAndConditions;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TermsAndConditionsMapper {

    @Mapping(target = "markdownText", source = "termsAndConditions.text")
    @Mapping(target = "lastUpdated", source = "termsAndConditions.lastModifiedDate")
    ResponseTermsAndConditions toResponse(TermsAndConditions termsAndConditions);

}
