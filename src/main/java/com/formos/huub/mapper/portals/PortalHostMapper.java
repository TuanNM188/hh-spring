package com.formos.huub.mapper.portals;

import com.formos.huub.domain.entity.PortalHost;
import com.formos.huub.domain.enums.PortalHostStatusEnum;
import com.formos.huub.domain.request.portals.RequestInvitePortalHost;
import com.formos.huub.domain.request.portals.RequestPortalHost;
import com.formos.huub.domain.response.portals.ResponseAboutPortalHost;
import com.formos.huub.domain.response.portals.ResponsePortalHost;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PortalHostMapper {

    PortalHost toEntity(RequestPortalHost requestPortalHost);

    @Mapping(target = "status", source = "status")
    PortalHost toEntityInvite(RequestInvitePortalHost request, PortalHostStatusEnum status);

    void partialEntity(@MappingTarget PortalHost portalHost, RequestPortalHost requestPortalHost);

    void partialEntityInvite(@MappingTarget PortalHost portalHost, RequestInvitePortalHost requestPortalHost);

    @Mapping(target = "fullName", expression = "java(convertFullName(portalHost))")
    ResponsePortalHost toResponse(PortalHost portalHost);

    default String convertFullName(PortalHost portalHost) {
        return String.format("%s %s", portalHost.getFirstName(), portalHost.getLastName());
    }
}
