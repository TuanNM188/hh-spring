package com.formos.huub.mapper.technicaladvisor;

import com.formos.huub.domain.entity.TechnicalAdvisor;
import com.formos.huub.domain.entity.User;
import com.formos.huub.domain.response.communitypartner.ResponseTechnicalAdvisor;
import com.formos.huub.domain.response.member.ResponseTechnicalAdvisorSetting;
import com.formos.huub.domain.response.technicaladvisor.IResponseTechnicalAdvisorSetting;
import com.formos.huub.domain.response.technicaladvisor.ResponseDetailBrowseAdvisor;
import com.formos.huub.mapper.communitypartner.CommunityPartnerMapper;
import com.formos.huub.mapper.portals.PortalMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
    uses = {
        CommunityPartnerMapper.class,
        PortalMapper.class
    },
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TechnicalAdvisorMapper {

    ResponseTechnicalAdvisorSetting toResponseFromInterface(IResponseTechnicalAdvisorSetting response);

    ResponseDetailBrowseAdvisor toResponseAdvisor(User user);


    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "status", source = "user.status")
    @Mapping(target = "imageUrl", source = "user.imageUrl")
    @Mapping(target = "normalizedFullName", source = "user.normalizedFullName")
    ResponseTechnicalAdvisor toResponseTechnicalAdvisor(TechnicalAdvisor technicalAdvisor);

}
