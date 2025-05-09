package com.formos.huub.mapper.funding;

import com.formos.huub.domain.entity.Funding;
import com.formos.huub.domain.response.funding.ResponseDetailFunding;
import com.formos.huub.framework.utils.ObjectUtils;
import com.formos.huub.mapper.categories.CategoryMapper;
import com.formos.huub.mapper.funder.FunderMapper;
import com.formos.huub.mapper.portals.PortalMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    uses = {
        CategoryMapper.class,
    },
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface FundingMapper {
    @Mapping(target = "fundingCategories", expression = "java(convertStringToList(funding.getFundingCategories()))")
    ResponseDetailFunding toResponse(Funding funding);

    default List<String> convertStringToList(String categories) {
        if (ObjectUtils.isEmpty(categories)) {
            return List.of();
        }
        return List.of(categories.split(","));
    }
}
