package com.formos.huub.mapper.portalfeature;

import com.formos.huub.domain.entity.Feature;
import com.formos.huub.domain.entity.Portal;
import com.formos.huub.domain.entity.PortalFeature;
import com.formos.huub.domain.request.portals.RequestPortalFeature;
import com.formos.huub.mapper.feature.FeatureMapper;
import org.mapstruct.*;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    uses = { FeatureMapper.class },
    builder = @Builder(disableBuilder = true),
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PortalFeatureMapper {

    @Mapping(target = "id.feature", source = "request.id")
    @Mapping(target = "id.portal", source = "portal")
    @Mapping(target = "isActive", source = "request.isActive")
    PortalFeature toEntity(RequestPortalFeature request, Portal portal);


    @Mapping(target = "id.feature", source = "feature")
    @Mapping(target = "id.portal", source = "portal")
    @Mapping(target = "isActive", source = "isActive")
    PortalFeature toObjectEntity(Portal portal, Feature feature, Boolean isActive);

}
