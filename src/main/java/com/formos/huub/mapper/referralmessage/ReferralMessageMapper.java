package com.formos.huub.mapper.referralmessage;

import com.formos.huub.domain.entity.ReferralMessage;
import com.formos.huub.domain.response.directmessage.ResponseDetailReferralMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReferralMessageMapper {


    @Mapping(target = "communityPartnerId", source = "communityPartner.id")
    @Mapping(target = "communityPartnerName", source = "communityPartner.name")
    ResponseDetailReferralMessage toResponse(ReferralMessage referralMessage);
}
