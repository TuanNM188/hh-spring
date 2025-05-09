package com.formos.huub.mapper.user;

import com.formos.huub.domain.entity.User;
import com.formos.huub.domain.request.user.RequestUpdateProfile;
import com.formos.huub.domain.response.technicalassistance.ResponseBasicInfoBusinessOwner;
import com.formos.huub.domain.response.user.ResponseProfile;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    void partialEntity(@MappingTarget User user, RequestUpdateProfile request);

    ResponseProfile toResponse(User user);

    ResponseBasicInfoBusinessOwner toResponseBasicInfoBusinessOwner(User user);

}
