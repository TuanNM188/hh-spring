package com.formos.huub.mapper.location;

import com.formos.huub.domain.entity.Location;
import com.formos.huub.domain.enums.LocationTypeEnum;
import com.formos.huub.domain.request.portals.RequestLocation;
import com.formos.huub.domain.response.location.ResponseLocationOption;
import com.formos.huub.domain.response.portals.ResponseLocation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LocationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "geoNameId", source = "requestLocation.geoNameId")
    @Mapping(target = "code", source = "requestLocation.code")
    @Mapping(target = "name", source = "requestLocation.name")
    @Mapping(target = "locationType", source = "locationType")
    Location toEntity(RequestLocation requestLocation, LocationTypeEnum locationType);

    ResponseLocation toResponse(Location location);

    @Mapping(target = "value", source = "location.code")
    @Mapping(target = "label", source = "location.name")
    ResponseLocationOption toResponseLocationOption(Location location);
}
