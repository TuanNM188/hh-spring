package com.formos.huub.mapper.portals;

import com.formos.huub.domain.entity.PortalState;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PortalStateMapper {

    @Mapping(target = "stateId", source = "stateId")
    @Mapping(target = "cities", source = "cities")
    @Mapping(target = "zipcodes", source = "zipcodes")
    @Mapping(target = "priorityOrder", source = "priorityOrder")
    PortalState toEntity(String stateId, String cities, String zipcodes, Integer priorityOrder);

    @Mapping(target = "stateId", source = "stateId")
    @Mapping(target = "cities", source = "cities")
    @Mapping(target = "zipcodes", source = "zipcodes")
    @Mapping(target = "priorityOrder", source = "priorityOrder")
    void partialEntity(@MappingTarget PortalState portalState, String stateId, String cities, String zipcodes, Integer priorityOrder);
}
