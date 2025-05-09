package com.formos.huub.mapper.portals;

import com.formos.huub.domain.entity.Funding;
import com.formos.huub.domain.request.portals.RequestCreatePortalFunding;
import com.formos.huub.domain.request.portals.RequestUpdatePortalFunding;
import com.formos.huub.domain.response.learninglibrary.ResponseSectionContent;
import com.formos.huub.domain.response.portals.ResponsePortalFundingDetail;
import com.formos.huub.framework.utils.ObjectUtils;
import com.formos.huub.framework.utils.StringUtils;
import com.formos.huub.mapper.categories.CategoryMapper;
import com.formos.huub.mapper.funder.FunderMapper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import liquibase.util.ObjectUtil;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    uses = {
        PortalMapper.class,
        CategoryMapper.class,
        FunderMapper.class
    },
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PortalFundingMapper {

    @Mapping(target = "funder", source = "request.funderId")
    @Mapping(target = "title", source = "request.portalFundingAbout.title")
    @Mapping(target = "status", source = "request.portalFundingAbout.status")
    @Mapping(target = "dateAdded", source = "request.portalFundingAbout.dateAdded")
    @Mapping(target = "publishDate", source = "request.portalFundingAbout.publishDate")
    @Mapping(target = "type", source = "request.portalFundingAbout.type")
    @Mapping(target = "amount", source = "request.portalFundingAbout.amount")
    @Mapping(target = "fundingCategories", expression = "java(convertListToString(request.getPortalFundingAbout().getFundingCategories()))")
    @Mapping(target = "description", source = "request.portalFundingAbout.description")
    @Mapping(target = "imageUrl", source = "request.portalFundingAbout.imageUrl")
    @Mapping(target = "hasDeadline", source = "request.portalFundingApplication.hasDeadline")
    @Mapping(target = "applicationDeadline", source = "request.portalFundingApplication.applicationDeadline")
    @Mapping(target = "applicationUrl", source = "request.portalFundingApplication.applicationUrl")
    @Mapping(target = "applicationProcess", source = "request.portalFundingApplication.applicationProcess")
    @Mapping(target = "applicationRequirement", source = "request.portalFundingApplication.applicationRequirement")
    @Mapping(target = "applicationRestriction", source = "request.portalFundingApplication.applicationRestriction")
    Funding toEntity(RequestCreatePortalFunding request);

    @Mapping(target = "funder", source = "request.funderId")
    @Mapping(target = "title", source = "request.portalFundingAbout.title")
    @Mapping(target = "status", source = "request.portalFundingAbout.status")
    @Mapping(target = "dateAdded", source = "request.portalFundingAbout.dateAdded")
    @Mapping(target = "publishDate", source = "request.portalFundingAbout.publishDate")
    @Mapping(target = "type", source = "request.portalFundingAbout.type")
    @Mapping(target = "amount", source = "request.portalFundingAbout.amount")
    @Mapping(target = "description", source = "request.portalFundingAbout.description")
    @Mapping(target = "imageUrl", source = "request.portalFundingAbout.imageUrl")
    @Mapping(target = "hasDeadline", source = "request.portalFundingApplication.hasDeadline")
    @Mapping(target = "applicationDeadline", source = "request.portalFundingApplication.applicationDeadline")
    @Mapping(target = "applicationUrl", source = "request.portalFundingApplication.applicationUrl")
    @Mapping(target = "applicationProcess", source = "request.portalFundingApplication.applicationProcess")
    @Mapping(target = "applicationRequirement", source = "request.portalFundingApplication.applicationRequirement")
    @Mapping(target = "applicationRestriction", source = "request.portalFundingApplication.applicationRestriction")
    @Mapping(target = "fundingCategories", expression = "java(convertListToString(request.getPortalFundingAbout().getFundingCategories()))")
    Funding partialUpdate(@MappingTarget Funding funding, RequestUpdatePortalFunding request);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "funderId", source = "funder.id")
    @Mapping(target = "portalFundingAbout.title", source = "title")
    @Mapping(target = "portalFundingAbout.status", source = "status")
    @Mapping(target = "portalFundingAbout.dateAdded", source = "dateAdded")
    @Mapping(target = "portalFundingAbout.publishDate", source = "publishDate")
    @Mapping(target = "portalFundingAbout.type", source = "type")
    @Mapping(target = "portalFundingAbout.amount", source = "amount")
    @Mapping(target = "portalFundingAbout.description", source = "description")
    @Mapping(target = "portalFundingAbout.imageUrl", source = "imageUrl")
    @Mapping(target = "portalFundingAbout.fundingCategories", expression = "java(convertStringToList(funding.getFundingCategories()))")
    @Mapping(target = "portalFundingApplication.hasDeadline", source = "hasDeadline")
    @Mapping(target = "portalFundingApplication.applicationDeadline", source = "applicationDeadline")
    @Mapping(target = "portalFundingApplication.applicationUrl", source = "applicationUrl")
    @Mapping(target = "portalFundingApplication.applicationProcess", source = "applicationProcess")
    @Mapping(target = "portalFundingApplication.applicationRequirement", source = "applicationRequirement")
    @Mapping(target = "portalFundingApplication.applicationRestriction", source = "applicationRestriction")
    ResponsePortalFundingDetail toResponseDetail(Funding funding);


    default String convertListToString(List<String> categories) {
        if (ObjectUtils.isEmpty(categories)){
            return StringUtils.EMPTY;
        }
        return String.join(",", categories);
    }

    default List<String> convertStringToList(String categories) {
        if (ObjectUtils.isEmpty(categories)){
            return List.of();
        }
        return List.of(categories.split(","));
    }
}
