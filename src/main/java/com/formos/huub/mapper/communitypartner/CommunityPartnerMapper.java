package com.formos.huub.mapper.communitypartner;

import com.formos.huub.domain.entity.CommunityPartner;
import com.formos.huub.domain.request.communitypartner.RequestCommunityPartnerAbout;
import com.formos.huub.domain.response.communitypartner.ResponseCommunityPartner;
import com.formos.huub.domain.response.communitypartner.ResponseCommunityPartnerAbout;
import com.formos.huub.domain.response.communityresource.ResponseDetailCommunityResource;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CommunityPartnerMapper {

    List<ResponseCommunityPartner> toListResponse(List<CommunityPartner> communityPartners);

    @Mapping(target = "portals", ignore = true)
    @Mapping(target = "serviceTypes", ignore = true)
    ResponseCommunityPartnerAbout toResponseAbout(CommunityPartner partner);

    @Mapping(target = "serviceTypes", ignore = true)
    @Mapping(target = "portals", ignore = true)
    CommunityPartner toEntity(RequestCommunityPartnerAbout request);

    @Mapping(target = "serviceTypes", ignore = true)
    @Mapping(target = "portals", ignore = true)
    void partialEntity(@MappingTarget CommunityPartner communityPartner, RequestCommunityPartnerAbout request);

    ResponseDetailCommunityResource toResponseDetailCommunityResource(CommunityPartner communityPartner);

}
