package com.formos.huub.mapper.eventregistration;

import com.formos.huub.domain.entity.EventRegistration;
import com.formos.huub.domain.response.eventregistration.ResponseEventRegistration;
import org.mapstruct.*;

import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EventRegistrationMapper {

    @Mapping(target = "businessOwnerId", source = "businessOwnerId")
    @Mapping(target = "registrationDate", source = "event.created")
    @Mapping(target = "registrationStatus", source = "event.status")
    EventRegistration toEntity(ResponseEventRegistration event, UUID businessOwnerId);

    @Mapping(target = "registrationDate", source = "request.created")
    @Mapping(target = "registrationStatus", source = "request.status")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void partialEntity(@MappingTarget EventRegistration event, ResponseEventRegistration request);
}
