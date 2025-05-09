package com.formos.huub.mapper.bookingsetting;

import com.formos.huub.domain.entity.Availability;
import com.formos.huub.domain.request.bookingsetting.RequestTechnicalAdvisorAvailability;
import com.formos.huub.domain.response.bookingsetting.ResponseAvailability;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AvailabilityMapper {
    Availability toEntity(RequestTechnicalAdvisorAvailability availability);

    void partialEntity(@MappingTarget Availability availability, RequestTechnicalAdvisorAvailability requestTechnicalAdvisorAvailability);

    List<ResponseAvailability> toResponseList(List<Availability> availabilities);
}
