package com.formos.huub.mapper.project;

import com.formos.huub.domain.entity.Project;
import com.formos.huub.domain.request.project.RequestCreateProject;
import com.formos.huub.domain.response.project.ResponseProject;
import com.formos.huub.domain.response.project.ResponseProjectHeader;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProjectMapper {

    @Mapping(target = "vendorName", source = "vendor.name")
    @Mapping(target = "portalName", source = "portal.platformName")
    @Mapping(target = "remainingAwardHours", source = "technicalAssistanceSubmit.remainingAwardHours")
    ResponseProject toResponse(Project project);

    @Mapping(target = "vendor", ignore = true)
    @Mapping(target = "businessOwner", ignore = true)
    @Mapping(target = "portal", ignore = true)
    @Mapping(target = "technicalAdvisor", ignore = true)
    @Mapping(target = "rating", expression = "java(getDefaultValueInteger())")
    @Mapping(target = "workAsExpected", expression = "java(getDefaultValueBoolean())")
    Project toEntity(RequestCreateProject request);

    default Integer getDefaultValueInteger() {
        return 0;
    }

    default Boolean getDefaultValueBoolean() {
        return false;
    }


    @Mapping(target = "advisorAvatar", source = "technicalAdvisor.user.imageUrl")
    @Mapping(target = "advisorId", source = "technicalAdvisor.user.id")
    @Mapping(target = "advisorFirstName", source = "technicalAdvisor.user.firstName")
    @Mapping(target = "advisorLastName", source = "technicalAdvisor.user.lastName")
    @Mapping(target = "communityPartnerName", source = "technicalAdvisor.communityPartner.name")
    @Mapping(target = "businessOwnerId", source = "businessOwner.user.id")
    @Mapping(target = "businessOwnerAvatar", source = "businessOwner.user.imageUrl")
    @Mapping(target = "businessOwnerFirstName", source = "businessOwner.user.firstName")
    @Mapping(target = "businessOwnerLastName", source = "businessOwner.user.lastName")
    ResponseProjectHeader toResponseProjectHeader(Project project);
}
